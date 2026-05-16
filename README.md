#  Chat TCP/UDP en Java
> Proyecto final - Redes - ITSON

Realizacion de una aplicación de chat Client - Servidor desarrollada en el entorno de java el cual permite la comunicación en tiempo real mediante sockets de TCP y UDP, cuenta con el soporte de mensajes grupales y privados entre múltiples clientes.



### Descripcion
Este sistema implementa una arquitectura cliente y servidor donde un servidor central gestiona las coneciones y retrasmite mensajes a los clientes que se esten conectados, se utilizan hilos independientes para un mejor manejo de cada cliente asi permitiendo varios usuarios se comuniquen simultáneamente sin restrincciones 

### Integrantes
| Nombre  | Matricula|
| ------------- | ------------- |
| Juan carlos valenzuela cabrera | 00000186288  |
| Marcelo Armenta Valenzuela | 00000262779  |

### Tecnologías implementadas

- Java 17+
- TCP Sockets -- conexión confiable y orientada a flijo de datos.
- UDP Sockets -- comunicación sin conexión de baja latencia.
- Multihilos -- manejo concurrente de clientes.
- Maven

# Estructura de proyecto
![image alt](https://github.com/Juan-Redes-Itson/Chat-tcp-udp-java/blob/d238fe767ba47970b23160aa5f8794cae1f6b9f5/Diagrama%20de%20chat%20TCP_UDP%20.drawio%20(1).png)

### como ejecutar

- Java JDK 17 o superior debe ser instalado
- Terminal o GitBash

#### compilar el proyecto
Javac -d bin C:\Users\JoshG\Documents\ChatTCPUDP\src\main\java\Clientes\ClientesTCP.java
Javac -d bin C:\Users\JoshG\Documents\ChatTCPUDP\src\main\java\Clientes\ClientesUDP.java
Javac -d bin C:\Users\JoshG\Documents\ChatTCPUDP\src\main\java\Servidores\ServidorTCP.java
Javac -d bin C:\Users\JoshG\Documents\ChatTCPUDP\src\main\java\Servidores\ServidorUDP.java

### Iniciar servidor

java -cp bin Servidores.TCPServer
java -cp bin Servidores.UDPServer

El servidor TCP se escuchara en el puerto 8888 y 9999 para los servidores UDP por defecto

### Conectar a un cliente 
java -cp bin Clientes.TCPCliente
java -cp bin Clientes.TCPCliente

se te pedira que ingreses tu nombre para poder acceder al servidor.

### Funcionalidades 
- Registro de usuario con su nombre único
- Mensajes grupales visibles para todos los que esten conectados
- Mensajes Privado entra usuariso (@Usuario : mensaje)
- Lista de usuarios conectados actualmente
- Desconexión verificada(exit)
- Historial de mensajes

# Capturas de pantalla
### Ingreso de usuario al servidor
![image alt](https://github.com/Juan-Redes-Itson/Chat-tcp-udp-java/blob/ebb456e3858f21ad14d3118aecc49e35119f6a4b/Ingreso%20de%20usuario%20.png)

### chat grupal
![image alt](https://github.com/Juan-Redes-Itson/Chat-tcp-udp-java/blob/7e7815986be4c1c098bb704688f29fe4a34c9c7b/chat%20grupal.png)
### chat privado 
![image alt](https://github.com/Juan-Redes-Itson/Chat-tcp-udp-java/blob/51e6e19737fb6195db875fdde5b93096a372e374/privado%20conversacion%20p1.png)
### chat privado respuesta
![image alt](https://github.com/Juan-Redes-Itson/Chat-tcp-udp-java/blob/51e6e19737fb6195db875fdde5b93096a372e374/conversacion%20privada%202.png)

### Protocolo de comunicación
los mensajes tienen el siguiente formato:
> 
[SERVIDOR] juan se unio al chat
> 
[USUARIOS] juan 
> HOLA A TODOS
> 
[juan] : HOLA A TODOS
>
> # Licencia
>- Proyecto académico -- ITSON - Redes - 2026

