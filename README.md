### Simple ClusterSharding project

While working on a different project I realized my ClusterSharding knowledge was lacking a few key elements so create this spin-off to gain a better understanding
of how to shard actors as well as create an http endpoint to access their state. This is based off the akka-samples project but I whittled it down to the bare bones in hopes it may help others.

Please feel free to make improvements if needed. Note that this is just Cluster Sharding and does not involve persistence so actor state is lost eventually once all nodes are down.

#### A three node cluster in separate JVMs

In the first terminal window, start the first seed node with the following command:

    sbt "runMain Application 2553"

2553 corresponds to the port of the first seed-nodes element in the configuration. In the log output you see that the cluster node has been started and changed status to 'Up'.

In the second terminal window, start the second seed node with the following command:

    sbt "runMain Application 2554"

2554 corresponds to the port of the second seed-nodes element in the configuration. In the log output you see that the cluster node has been started and joins the other seed node and becomes a member of the cluster. Its status changed to 'Up'. Switch over to the first terminal window and see in the log output that the member joined.

Some of the temperature aggregators that were originally on the `ActorSystem` on port 2553 will be migrated to the newly joined `ActorSystem` on port 2554. The migration is straightforward: the old actor is stopped and a fresh actor is started on the newly created `ActorSystem`. 

Start another node in the third terminal window with the following command:

    sbt "runMain Application 0"

Now you don't need to specify the port number, 0 means that it will use a random available port. It joins one of the configured seed nodes.
Look at the log output in the different terminal windows.

Start even more nodes in the same way, if you like.

#### API

With the cluster running you can interact with the HTTP endpoint using raw HTTP, for example with `curl`.

Record data for thermostat 1234:

```
curl -XPOST http://localhost:12553/thermostat/1234 -H "Content-Type: application/json" --data '{"temperature": "21.0"}'
```

Query temperature for thermostat 1234:

```
curl "http://localhost:12553/thermostat/1234"
```

You can also change the port to any of the other nodes and run the same query and you will get a result in return. Even if the node isn't responsible for handling that particular
thermostat Akka will be able to route the message to the right node.