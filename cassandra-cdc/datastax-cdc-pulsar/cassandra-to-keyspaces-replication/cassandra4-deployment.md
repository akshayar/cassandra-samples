# Deploying Cassandra
1. For Apache Cassandra deployment we are following this [link](https://aws-quickstart.github.io/quickstart-datastax-oss/).  
2. Refer to the [overview](https://aws-quickstart.github.io/quickstart-datastax-oss/#_overview) and ensure that the deployment is supported in your region. 
3. Use quick start [link](https://aws-quickstart.github.io/quickstart-datastax-oss/#_launch_the_quick_start) to launch Cassandra 4 cluster into an existing VPC or a new VPC.
4. Vaidate the deployment by connecting to a node of the cluster through bastion node ( jump box) and executing `nodetool status`

