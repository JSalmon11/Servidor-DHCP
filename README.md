# Servidor-DHCP
Servidor básico DHCP, responde a las peticiones Discover y a Request, no renueva ips y solo configura un cliente.

## Comenzando 🚀

Puedes decargar el código y ejecutarlo en el IDE Java que quieras.

### Pre-requisitos 📋

-Un IDE de programación Java, el programa está hecho en NetbBeans, por lo que sería la mejor opción para ejecutarlo en tu pc sin problemas.

-Mínimo la versión 8 de Java, el programa usa el Jdk 1.8.

### Instalación 🔧

Puedes decargar el código o el proyecto entero.
En ambos casos si usas un IDE distinto de NetBeans deberás revisar por si pudiera dar problemas con la codificación UTF8, aunque he intentado no usar tildes ni ñ para evitar ese error.

## Ejecución ⚙️

Para poder ejecutarlo necesitas tener este proyecto en una máquina con la misma red de la máquina que hará de cliente. Por ejemplo para hacerlo con máquinas virtuales, se crea una red con ip 10.0.2.0/24
(es la que está configurada en el código, la puedes cambiar si quieres, pero en ambos sitios), quitando el servicio de servidor de DHCP en esa red creada, ahora enciendes la máquina servidor y arrancas el programa,
enciendes la máquina cliente y se configurará gracias al servidor, el servidor se detiene una vez configurado un cliente.

También se podriía hacer sin máquinas virtuales, desactivando el servicio de DHCP de tu router.

## Construido con 🛠️

* [NetBeans](https://netbeans.apache.org/download/index.html) - El IDE usado
* [Java 8(jdk1.8)](https://www.java.com/es/download/ie_manual.jsp) - Versión de Java utilizada
