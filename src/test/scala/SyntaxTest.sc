val topic = "$SYS/requestors/1"

topic.split("/").last
topic.split("/").dropRight(1).mkString("/")