# Apache Pulsar multi node deployment
1. You will use Terraform and Ansible to deploy the cluster. You will use Cloud9 instance to execute the steps mentioned below. You need to create the Cloud9 workspace in a public subnet of the target VPC. The VPC could be the VPC of Aapche Cassandra or a VPC that has a peering connection with VPC of Apache Cassandra. 
2. Create a Cloud9 workspace in the public subnet of the VPC of Apache Cassandra.
3. Connect to the Cloud9 workspace and on a terminal execute following commands to install ansible-
```shell
sudo yum install jq ; pip install ansible
terraform version
aws configure
```
4. Install Terraform Inventory tool which will create ansible inventory from terraform deployment.
```shell
wget https://github.com/adammck/terraform-inventory/releases/download/v0.10/terraform-inventory_v0.10_linux_amd64.zip
unzip terraform-inventory_v0.10_linux_amd64.zip 
chmod +x terraform-inventory
export PATH=$PATH:./
```
5. Create ssh keys
```shell
ssh-keygen -t rsa  

ls ~/.ssh
```
6. Get subnet and VPC details of Cloud9 instance.
```shell
INSTANCE_ID=`wget -q -O - http://169.254.169.254/latest/meta-data/instance-id`
echo ${INSTANCE_ID}
SECURITY_GROUP_ID=`aws ec2 describe-instance-attribute --instance-id $INSTANCE_ID --attribute groupSet --query Groups[0].GroupId --output text`
VPC_ID=`aws ec2 describe-instances   --instance-ids ${INSTANCE_ID} --query Reservations[0].Instances[0].VpcId --output text`
aws ec2 describe-instances   --instance-ids ${INSTANCE_ID} --query Reservations[0].Instances[0].[PublicDnsName,PublicIpAddress,PrivateDnsName,PrivateIpAddress,SubnetId,VpcId] --output text
aws ec2 describe-vpcs  --vpc-ids ${VPC_ID} --query Vpcs[0].CidrBlock --output text
aws ec2 describe-subnets --filters "Name=vpc-id,Values=${VPC_ID}" --query Subnets[].[VpcId,AvailabilityZone,CidrBlock,SubnetId,AvailableIpAddressCount] --output text

```

7. Edit terraform.tfvars file and update region,VpcID, availability_zone , subnet_id and base_cidr_block.
8. Execute deployment steps. The deployment steps will promopt-"Do you want to perform these actions?". Enter yes.
```shell
cd ${AWS_DEPLOYMENT_HOME}
terraform init
terraform apply
```

```
PULSAR_SERVICE_VALUE=`cat terraform.tfstate | jq -r .outputs.pulsar_service_url.value` 
echo $PULSAR_SERVICE_VALUE 
PULSAR_ADMIN_URL=`cat terraform.tfstate | jq -r .outputs.pulsar_web_url.value` 
echo $PULSAR_ADMIN_URL
export REGION="us-east-1"
export SOURCE_KEYSPACE="pocdb1"
export SOURCE_TABLE_NAME="customers"
```
```
export CASSANDRA_SERVERS=<cassandra_servers>

```
```
envsubst < ../parameters/cassandra-config-template.json > ../parameters/cassandra-config.json
```

9. Run the ansible playbook to deploy pulsar on EC2 instances created above.
```shell
TF_STATE=./ TF_KEY_NAME=private_ip ansible-playbook   --user='ec2-user'   --inventory=~/environment/terraform-inventory  ../pulsar-cluster-deploy.yaml
```
11. Validate Apache Pulsar cluster  installation. 
```shell
mkdir ~/environment/pulsar-client
pip install pulsar-client
cd ~/environment/pulsar-client
bin/pulsar-client --url $PULSAR_SERVICE_VALUE  consume test-topic -s "first-subscription" &

bin/pulsar-client --url $PULSAR_SERVICE_VALUE  produce test-topic --messages "hello-pulsar" > test-produce.log

```
### Appendix
1. Check status of Pulsar deployment. 
   
```
cd ${AWS_DEPLOYMENT_HOME}
TF_STATE=./ TF_KEY_NAME=private_ip ansible-playbook   --user='ec2-user'   --inventory=~/environment/terraform-inventory  ../pulsar-cluster-check-services.yaml

```
2. Restart pulsar services.

```
cd ${AWS_DEPLOYMENT_HOME}
TF_STATE=./ TF_KEY_NAME=private_ip ansible-playbook   --user='ec2-user'   --inventory=~/environment/terraform-inventory  ../pulsar-cluster-restart-services.yaml

```

3. Getting instance id and its security group
```shell
INSTANCE_ID=`wget -q -O - http://169.254.169.254/latest/meta-data/instance-id`
echo ${INSTANCE_ID} 
SECURITY_GROUP_ID=`aws ec2 describe-instance-attribute --instance-id $INSTANCE_ID --attribute groupSet --query Groups[0].GroupId --output text`
VPC_ID=`aws ec2 describe-instances   --instance-ids ${INSTANCE_ID} --query Reservations[0].Instances[0].VpcId --output text`
aws ec2 describe-vpcs  --vpc-ids ${VPC_ID} --query Vpcs[0].CidrBlock --output text
aws ec2 describe-subnets --filters "Name=vpc-id,Values=${VPC_ID}" --query Subnets[].[VpcId,AvailabilityZone,CidrBlock,SubnetId,AvailableIpAddressCount] --output text
```

