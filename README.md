# proxmox-ve-api-java-mock
A Mockito based mock for LUMASERV/proxmox-ve-api-java

## Maven
```xml
<repository>
    <id>lumaserv</id>
    <url>https://maven.lumaserv.cloud</url>
</repository>
```
```xml
<dependency>
    <groupId>com.lumaserv</groupId>
    <artifactId>proxmox-ve-api-mock</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Usage
```java
MockState state = new MockState();
state.createNode("example");
ProxMoxVEClient client = new ProxMoxVEMock.create(state);
client.getNodes().forEach(node -> System.out.println(node.getName()));
```