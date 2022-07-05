# Apache Pulsar multi node deployment
1. You will use Terraform and Ansible to deploy the cluster. You will use Cloud9 instance to execute the steps mentioned below. You need to create the Cloud9 workspace in a public subnet of the target VPC. The VPC could be the VPC of Aapche Cassandra or a VPC that has a peering connection with VPC of Apache Cassandra.

2. Install Terraform Inventory tool which will create ansible inventory from terraform deployment.
```shell
cd $HOME/environment
wget https://github.com/adammck/terraform-inventory/releases/download/v0.10/terraform-inventory_v0.10_linux_amd64.zip
unzip terraform-inventory_v0.10_linux_amd64.zip 
chmod +x terraform-inventory
export PATH=$PATH:./
```
3. Create ssh keys
```shell
ssh-keygen -t rsa  

ls ~/.ssh
```
4. Get subnet and VPC details of Cloud9 instance.
```shell
INSTANCE_ID=`wget -q -O - http://169.254.169.254/latest/meta-data/instance-id`
echo ${INSTANCE_ID}
SECURITY_GROUP_ID=`aws ec2 describe-instance-attribute --instance-id $INSTANCE_ID --attribute groupSet --query Groups[0].GroupId --output text`
VPC_ID=`aws ec2 describe-instances   --instance-ids ${INSTANCE_ID} --query Reservations[0].Instances[0].VpcId --output text`
aws ec2 describe-instances   --instance-ids ${INSTANCE_ID} --query Reservations[0].Instances[0].[PublicDnsName,PublicIpAddress,PrivateDnsName,PrivateIpAddress,SubnetId,VpcId] --output text
aws ec2 describe-vpcs  --vpc-ids ${VPC_ID} --query Vpcs[0].CidrBlock --output text
aws ec2 describe-subnets --filters "Name=vpc-id,Values=${VPC_ID}" --query Subnets[].[VpcId,AvailabilityZone,CidrBlock,SubnetId,AvailableIpAddressCount] --output text

```

5. Create Pulsar nodes in the private subnet of the VPC of Cassandra cluster. Edit terraform.tfvars file and update region,VpcID, availability_zone , subnet_id and base_cidr_block.
6. Execute following command to create EC2 instnaces required to host Apache Pulsar. The deployment steps will promopt-"Do you want to perform these actions?". Enter yes.
```shell
cd ${AWS_DEPLOYMENT_HOME}
terraform init
terraform apply
```
7. Set environment variables and use those to create the config file which will be used further. 
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

8. Run the ansible playbook to deploy Apache Pulsar on EC2 instances created above.
```shell
TF_STATE=./ TF_KEY_NAME=private_ip ansible-playbook   --user='ec2-user'   --inventory=~/environment/terraform-inventory  ../pulsar-cluster-deploy.yaml
```
9. Validate Apache Pulsar cluster  installation. The steps above will install Apache Pulsar libraries on Cloud9 node at ~/environment/pulsar-client. Validate cluster deployment by sending and consuming messages. 
```shell
mkdir ~/environment/pulsar-client
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

