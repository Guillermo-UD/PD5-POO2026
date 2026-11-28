# Sistema de Gestión de Stands - FILEY UADY (PD5)

Sistema desarrollado en Java bajo el patrón de arquitectura **Modelo-Vista-Controlador (MVC)** integrado con una base de datos relacional mediante **JDBC**. La aplicación implementa las operaciones CRUD para la gestión de las entidades principales de FILEY UADY: **Stands**, **Editoriales** y **Reservaciones**.

se incluyen los diagramas de entidades-relacion y conexiones DB, hechas con la herramienta MERMAID
---

## Características Implementadas

* **Arquitectura MVC:** Separación clara entre la capa de datos/negocio, las interfaces de usuario y la lógica de control.
* **Persistencia con JDBC:** Conexión directa a la base de datos para realizar consultas, lecturas y operaciones CRUD sobre las entidades del sistema.
* **Estrategia de División CRUD:** Operaciones modulares y separadas por entidad (Stands, Editorial y Reservación), desacoplando la gestión de conexiones de las consultas específicas.
* **Manejo de Excepciones:** Control estructurado de errores y excepciones personalizadas dentro del paquete del modelo para validar reglas de negocio y fallos de conexión o consulta.
* **Demostración en Ejecución:** La clase principal (`Main`) verifica la conexión exitosa a la base de datos y ejecuta pruebas funcionales de las operaciones CRUD para cada entidad.

---

## Enlace al video explicativo

* [Ver demostración del sistema en YouTube](AQUI_VA_TU_ENLACE_DE_YOUTUBE)

---

## Acceso Directo al Código Fuente

* **Proyecto Maven:** [`STANDS/`](STANDS/)
* **Archivo de configuración Maven:** [`STANDS/pom.xml`](STANDS/pom.xml)
* **Paquete fuente principal:** [`STANDS/src/main/java/filey/`](STANDS/src/main/java/filey/)
* **Recursos:** [`STANDS/src/main/resources/`](STANDS/src/main/resources/)
* **Clase Principal:** [`Main.java`](STANDS/src/main/java/filey/Main.java)
* **Controlador:** [`controlador/`](STANDS/src/main/java/filey/controlador/)
* **Modelo:** [`modelo/`](STANDS/src/main/java/filey/modelo/)
* **Vista:** [`vista/`](STANDS/src/main/java/filey/vista/)

---

## Diagramas de Arquitectura y Base de Datos

Para visualizar los diagramas con mayor detalle, se incluyen los archivos originales en formato PDF en la raíz del repositorio:

### 1. Diagrama de Clases (Conexiones y Operaciones BD)
* [Abrir Diagrama de Clases en PDF](diagrama%20de%20clases%20bd.pdf)

![Diagrama de Clases BD](diagrama%20de%20clases%20bd.png)

---

### 2. Diagrama Entidad-Relación
* [Abrir Diagrama Entidad-Relación en PDF](diagrama%20entidad-relacion.pdf)

![Diagrama Entidad-Relación](diagrama%20entidad-relacion.png)
