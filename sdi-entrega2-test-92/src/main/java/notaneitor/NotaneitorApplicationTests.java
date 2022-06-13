package notaneitor;

import notaneitor.pageobjects.*;
import notaneitor.util.MongoUtils;
import notaneitor.pageobjects.PO_FriendRequestView;
import notaneitor.util.SeleniumUtils;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.Integer.parseInt;
import static org.junit.jupiter.api.Assertions.assertTrue;


//Ordenamos las pruebas por la anotación @Order de cada método
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NotaneitorApplicationTests {
    //Para MACOSX
    //static String PathFirefox = "/Applications/Firefox 2.app/Contents/MacOS/firefox-bin";
    //static String Geckodriver = "/Users/delacal/selenium/geckodriver-v0.30.0-macos";
    //Para Windows
    //static String Geckodriver = "C:\\Users\\david\\OneDrive\\Documentos\\SDI21-22\\PL-SDI-Sesión5-material\\geckodriver-v0.30.0-win64.exe";
    static String PathFirefox = "C:\\Program Files\\Mozilla Firefox\\firefox.exe";
    // Rosa
  //  static String Geckodriver = "C:\\Users\\rosa_\\Documents\\Uni\\3º\\Segundo cuatri\\SDI\\Lab\\sesion05\\PL-SDI-Sesión5-material\\geckodriver-v0.30.0-win64.exe";
    // David
    //static String Geckodriver = "C:\\Users\\david\\OneDrive\\Documentos\\SDI21-22\\PL-SDI-Sesión5-material\\geckodriver-v0.30.0-win64.exe";
    //Mateo
  static String Geckodriver ="C:\\Users\\User\\Desktop\\TERCERO\\SDI\\sesion06\\PL-SDI-Sesión5-material\\geckodriver-v0.30.0-win64.exe";
    // Geckodriver María
   // static String Geckodriver = "C:\\Program Files\\geckodriver-v0.30.0-win64.exe";
    //Miguel
    //static String Geckodriver ="C:\\Users\\migue\\OneDrive\\Documentos\\Uniovi\\Tercero segundo cuatri\\SDI\\Practica\\geckodriver-v0.30.0-win64.exe";

    //Común a Windows y a MACOSX
    static final String URL = "http://localhost:8081";
    static WebDriver driver = getDriver(PathFirefox, Geckodriver);
    static MongoUtils mongo= new MongoUtils();

    public static WebDriver getDriver(String PathFirefox, String Geckodriver) {
        System.setProperty("webdriver.firefox.bin", PathFirefox);
        System.setProperty("webdriver.gecko.driver", Geckodriver);
        driver = new FirefoxDriver();
        return driver;
    }

    //Antes de la primera prueba
    @BeforeAll
    static public void begin() {
        //mongo.borrarUsuarios();
        //mongo.crearUsers();
        //mongo.crearPosts();
    }

    //Al finalizar la última prueba
    @AfterAll
    static public void end() {
        //Borramos la amistad entre estos 2 usuarios para que vuelvan a ejecutarese los tests correctamente
        //mongo.deleteFriendship("user11@email.com","user09@email.com");
       // mongo.borrarUsuarios();
        //mongo.crearUsers();
        //Cerramos el navegador al finalizar las pruebas
        driver.quit();
        mongo.closeClient();
    }

    //Antes de cada prueba se navega al URL home de la aplicación
    @BeforeEach
    public void setUp() {
        driver.navigate().to(URL);
    }

    //Después de cada prueba se borran las cookies del navegador
    @AfterEach
    public void tearDown() {
        driver.manage().deleteAllCookies();
        //driver.close();
    }

    public void registrarUser(String email,String pass){

        PO_HomeView.clickOption(driver, "signup", "class", "btn btn-primary");
        //Rellenamos el formulario.
        PO_SignUpView.fillForm(driver, email, "Mateo", "Perez", pass, pass);

    }

    public void enviarPeticion(String email,String pass){
        // Se entra en la aplicación como usuario1 (ROL --> Usuario)
        PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
        PO_LoginView.fillLoginForm(driver, email, pass);

        // Enviamos petición al primer usuario de la lista
        By boton = By.id("btnPeticion");
        driver.findElement(boton).click();


        // Nos desconectamos y conectamos con el test5 que es al que hemos enviado la solicitud
        PO_HomeView.clickOption(driver, "logout", "class", "btn btn-primary");

    }
    //PR01. Prueba del formulario de registro. registro con datos correctos
    @Test
    @Order(1)
    void PR01() {

        registrarUser("borrar1@email.com","borrar1");
        //Vamos al formulario de registro
        PO_HomeView.clickOption(driver, "signup", "class", "btn btn-primary");
        //Rellenamos el formulario.
        PO_SignUpView.fillForm(driver, "test24@email.com", "Mateo", "Perez", "test24", "test24");
        //Comprobamos que se notifica el registro.

        String checkText = "Usuario registrado.";
        List<WebElement> result = PO_View.checkElementBy(driver, "class", "alert alert-info");
        Assertions.assertEquals(checkText, result.get(0).getText());


    }

    //PR02. Prueba del formulario de registro. Campos vacíos
    // Propiedad: Error.xxx.empty
    @Test
    @Order(2)
    void PR02() {
        PO_HomeView.clickOption(driver, "signup", "class", "btn btn-primary");
        PO_SignUpView.fillForm(driver, "", "", "", "77777", "77777");
        // En este caso, comprobamos que no se ha registrado el usuario y no se redirige a otra página;
        // ya que los campos son requeridos
        String checkText = "";
        List<WebElement> result = PO_View.checkElementBy(driver, "id", "nombre");
        Assertions.assertEquals(checkText, result.get(0).getText());
    }

    //PR03. Prueba del formulario de registro. Repetición de contraseña inválida.
    // Propiedad: Error.signup.passwordConfirm.coincidence
    @Test
    @Order(3)
    void PR03() {
        PO_HomeView.clickOption(driver, "signup", "class", "btn btn-primary");
        PO_SignUpView.fillForm(driver, "test1@email.com", "Jose", "Perez", "test", "test1");
        //Comprobamos el error de contraseña repetida inválida
        String checkText = "Las contraseñas no coinciden";
        List<WebElement> result = PO_View.checkElementBy(driver, "class", "alert alert-danger");
        Assertions.assertEquals(checkText, result.get(0).getText());
    }

    //PR04. Prueba del formulario de registro. Email existente.
    // Propiedad: Error.signup.email.duplicate
    @Test
    @Order(4)
    public void PR04() {
        PO_HomeView.clickOption(driver, "signup", "class", "btn btn-primary");
        PO_SignUpView.fillForm(driver, "test24@email.com", "Pedro", "Lopez", "test24", "test24");
        //Comprobamos el error de email duplicado.
        String checkText = "Email en uso.";
        List<WebElement> result = PO_View.checkElementBy(driver, "class", "alert alert-info");
        Assertions.assertEquals(checkText, result.get(0).getText());
    }

    // Identificación válida con usuario de ROL Administrador (admin@email.com/admin)
    @Test
    @Order(5)
    public void PR05() {
        //Vamos al formulario de logueo.
        PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
        //Rellenamos el formulario
        PO_LoginView.fillLoginForm(driver, "admin@email.com", "admin");
        //Comprobamos que puede ver los usuarios
        String checkText = "Usuarios";
        List<WebElement> result = PO_View.checkElementBy(driver, "id", "users");
        Assertions.assertEquals(checkText, result.get(0).getText());
    }

    // Identificación válida con usuario de ROL usuario (user01@email.com/user01)
    @Test
    @Order(6)
    public void PR06() {
        //Vamos al formulario de logueo.
        PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
        //Rellenamos el formulario
        PO_LoginView.fillLoginForm(driver, "test24@email.com", "test24");
        //Comprobamos que podemos ver sus amigos
        String checkText = "Amigos";
        List<WebElement> result = PO_View.checkElementBy(driver, "id", "friends");
        Assertions.assertEquals(checkText, result.get(0).getText());
    }

    // Inicio de sesión con datos inválidos (usuario estándar, campo email y contraseña vacíos).
    @Test
    @Order(7)
    public void PR07() {
        //Vamos al formulario de logueo.
        PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
        //Rellenamos el formulario
        PO_LoginView.fillLoginForm(driver, "", "");
        //Comprobamos que no se ha iniciado sesión
        String checkText = "Identificación de usuario";
        List<WebElement> result = PO_View.checkElementBy(driver, "text", checkText);
        Assertions.assertEquals(checkText, result.get(0).getText());
        //comprobamos que el text de identificacion sigue en pantalla por lo que no se inicio sesion con datos erroneos
    }

    // Inicio de sesión con datos inválidos
    // (usuario estándar, email existente, pero contraseña incorrecta)
    @Test
    @Order(8)
    public void PR08() {
        //Vamos al formulario de logueo.
        PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
        //Rellenamos el formulario
        PO_LoginView.fillLoginForm(driver, "test@email.com", "s");
        //Comprobamos que no se ha iniciado sesión


        String checkText = "Email o password incorrecto";
        List<WebElement> result = PO_View.checkElementBy(driver, "class", "alert alert-danger");
        Assertions.assertEquals(checkText, result.get(0).getText());
    }

    // Hacer clic en la opción de salir de sesión y comprobar que se redirige a la página de inicio de sesión (Login).
    @Test
    @Order(9)
    public void PR09() {
        //Vamos al formulario de logueo.
        PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
        //Rellenamos el formulario
        PO_LoginView.fillLoginForm(driver, "test24@email.com", "test24");
        //Se desconecta de la sesión
        PO_HomeView.clickOption(driver, "logout", "class", "btn btn-primary");
        //Comprobamos que se ha desconectado
        String checkText = "Identifícate";
        List<WebElement> result = PO_View.checkElementBy(driver, "text", checkText);
        Assertions.assertEquals(checkText, result.get(0).getText());
    }

    //Comprobar que el botón cerrar sesión no está visible si el usuario no está autenticado.
    @Test
    @Order(10)
    public void PR10() {
        // Comprobamos que no aparece la opción al entrar en la página ya que podemos iniciar sesion
        PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
        PO_LoginView.fillLoginForm(driver, "test24@email.com", "test24");
        //Comprobamos que al conectarse si sale
        String checkText = "Cerrar sesión";
        List<WebElement> result = PO_View.checkElementBy(driver, "text", checkText);
        Assertions.assertEquals(checkText, result.get(0).getText());
    }

    //Mostrar el listado de usuarios y comprobar que se muestran todos los que existen en el sistema.
    @Test
    @Order(11)
    public void PR11() {
        //Vamos al formulario de logueo.
        PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
        //Rellenamos el formulario
        PO_LoginView.fillLoginForm(driver, "admin@email.com", "admin");
        //Comprobamos que puede ver los usuarios
        String checkText = "Usuarios";
        List<WebElement> result = PO_View.checkElementBy(driver, "id", "idTitleAdminListUser");
        Assertions.assertEquals(checkText, result.get(0).getText());
        Assertions.assertTrue(PO_AdminUsersListView.countUsers(driver) > 0);
    }

    // Borrado del primer usuario de la lista
    @Test
    @Order(12)
    public void PR12() {
        //Se entra en la aplicación con rol de Administrador
        PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
        PO_LoginView.fillLoginForm(driver, "admin@email.com", "admin");
        // Guardamos el número de usuarios actual y el primer usuario
        int usersBefore = PO_AdminUsersListView.countUsers(driver);
        WebElement userToRemove = PO_AdminUsersListView.getUser(driver,0);
        // Se selecciona el primer checkBox
        List<Integer> positions = new ArrayList<>();
        positions.add(0);
        PO_AdminUsersListView.markCheckBoxElements(driver,positions);
        // Se pulsa el botón eliminar
        PO_AdminUsersListView.clickBtn(driver);
        // Se comprueba que el tamaño de la lista es un elemento menor
        int usersAfter = PO_AdminUsersListView.countUsers(driver);
        Assertions.assertEquals(usersAfter, (usersBefore-1));
        // y que el usuario ya no está en la lista
        Assertions.assertFalse(PO_AdminUsersListView.checkUserInList(driver, userToRemove));
    }

    // Borrado del último usuario de la lista
    @Test
    @Order(13)
    public void PR13() {
        registrarUser("borrar2@email.com","borrar2");
        //Se entra en la aplicación con rol de Administrador
        PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
        PO_LoginView.fillLoginForm(driver, "admin@email.com", "admin");
        // Guardamos el número de usuarios actual y el último usuario
        int usersBefore = PO_AdminUsersListView.countUsers(driver);
        WebElement userToRemove = PO_AdminUsersListView.getUser(driver,usersBefore-1);
        // Se selecciona el último checkBox
        List<Integer> positions = new ArrayList<>();
        positions.add(usersBefore-1);
        PO_AdminUsersListView.markCheckBoxElements(driver,positions);
        // Se pulsa el botón eliminar
        PO_AdminUsersListView.clickBtn(driver);
        // Se comprueba que el tamaño de la lista es un elemento menor
        int usersAfter = PO_AdminUsersListView.countUsers(driver);
        Assertions.assertEquals(usersAfter, (usersBefore-1));
        // y que el usuario ya no está en la lista
        Assertions.assertFalse(PO_AdminUsersListView.checkUserInList(driver, userToRemove));
    }

    // Borrado de 3 usuarios de la lista
    @Test
    @Order(14)
    public void PR14() {

        registrarUser("borrar1@email.com","borrar1");
        registrarUser("borrar2@email.com","borrar2");
        registrarUser("borrar3@email.com","borrar3");
        //Se entra en la aplicación con rol de Administrador
        PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
        PO_LoginView.fillLoginForm(driver, "admin@email.com", "admin");
        // Guardamos el número de usuarios actual y los 3 usuarios a borrar (posiciones 1, 2 y 3)
        int usersBefore = PO_AdminUsersListView.countUsers(driver);
        WebElement user1ToRemove = PO_AdminUsersListView.getUser(driver,1);
        WebElement user2ToRemove = PO_AdminUsersListView.getUser(driver,2);
        WebElement user3ToRemove = PO_AdminUsersListView.getUser(driver,3);
        // Se selecciona el último checkBox
        List<Integer> positions = new ArrayList<>();
        positions.add(1);
        positions.add(2);
        positions.add(3);
        PO_AdminUsersListView.markCheckBoxElements(driver,positions);
        // Se pulsa el botón eliminar
        PO_AdminUsersListView.clickBtn(driver);
        // Se comprueba que el tamaño de la lista es un elemento menor
        int usersAfter = PO_AdminUsersListView.countUsers(driver);
        Assertions.assertEquals(usersAfter, (usersBefore-3));
        // y que el usuario ya no está en la lista
        Assertions.assertFalse(PO_AdminUsersListView.checkUserInList(driver, user1ToRemove));
        Assertions.assertFalse(PO_AdminUsersListView.checkUserInList(driver, user2ToRemove));
        Assertions.assertFalse(PO_AdminUsersListView.checkUserInList(driver, user3ToRemove));
    }

    @Test
    @Order(15)
    public void PR15() {
        registrarUser("test5@email.com","test5");
        //Vamos al formulario de logueo.
        PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
        //Rellenamos el formulario
        PO_LoginView.fillLoginForm(driver, "test24@email.com", "test24");


        // Comprobamos todos los usuarios
        PO_PrivateView.checkUser(driver, "test5@email.com");

    }


    @Test
    @Order(16)
    public void PR16() {
        //Vamos al formulario de logueo.
        PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
        //Rellenamos el formulario
        PO_LoginView.fillLoginForm(driver, "test24@email.com", "test24");



        // Estando en el menu buscamos sin rellenar el campo de texto
        PO_SearchView.fillForm(driver,"");

        // Comprobamos todos los usuarios
        PO_PrivateView.checkUser(driver, "test5@email.com");

    }

    @Test
    @Order(17)
    public void PR17() {
        //Vamos al formulario de logueo.
        PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
        //Rellenamos el formulario
        PO_LoginView.fillLoginForm(driver, "test24@email.com", "test24");



        // Estando en el menu buscamos sin rellenar el campo de texto
        PO_SearchView.fillForm(driver,"admin");

        //Comprobacion pagina 1 0 elementos
        SeleniumUtils.waitTextIsNotPresentOnPage(driver,"user",PO_View.getTimeout());
    }

    @Test
    @Order(18)
    public void PR18() {
        //Vamos al formulario de logueo.
        PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
        //Rellenamos el formulario
        PO_LoginView.fillLoginForm(driver, "test24@email.com", "test24");


       //Rellenamos con test5 y debería salirnos solo 1 usuario
        PO_SearchView.fillForm(driver,"test5");

        //Comprobacion pagina 1 1 elementos
        List<WebElement> page1 =  SeleniumUtils.waitLoadElementsBy(driver,"free", "//tbody/tr",PO_View.getTimeout());
        assertTrue(page1.size()== 1);
    }

    // Envía una invitación de amistad a un usuario
    @Test
    @Order(19)
    public void PR19() {
        // Se entra en la aplicación como usuario1 (ROL --> Usuario)
        PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
        PO_LoginView.fillLoginForm(driver, "test24@email.com", "test24");

        // Enviamos petición al primer usuario de la lista
        By boton = By.id("btnPeticion");
        driver.findElement(boton).click();


        // Nos desconectamos y conectamos con el test5 que es al que hemos enviado la solicitud
        PO_HomeView.clickOption(driver, "logout", "class", "btn btn-primary");

        PO_LoginView.fillLoginForm(driver, "test5@email.com", "test5");
        // Vamos a la vista de lista de solicitudes de amistad
        PO_HomeView.clickOption(driver, "/user/friendRequests", "id", "friendRequests");
        // Comprobamos que aparece el usuario 1
        PO_PrivateView.checkUser(driver,"Mateo");
    }

    // Enviar una petición de amistad a un usuario al que ya le habíamos mandado la petición previamente
    @Test
    @Order(20)
    public void PR20() {
        registrarUser("borrar@email.com","borrar");



        // Se entra en la aplicación como usuario11 (ROL --> Usuario)
        PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
        PO_LoginView.fillLoginForm(driver, "test24@email.com", "test24");
        //solo tendria de user disponible al test5 y  test6 por lo que solo apareceria un boton de solicitud


        List<WebElement> result = PO_View.checkElementBy(driver, "id", "btnPeticion");
        Assertions.assertEquals(1, result.size());




    }

    //Mostrar el listado de invitaciones de amistad recibidas. Comprobar con un listado que
    //contenga varias invitaciones recibidas
    @Test
    @Order(21)
    public void PR21() {
        enviarPeticion("test24@email.com","test24");
        //Vamos al formulario de logueo.
        PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
        //Rellenamos el formulario
        PO_LoginView.fillLoginForm(driver, "test5@email.com", "test5");

        //vamos al apartado de friendRequests
        PO_HomeView.clickOption(driver, "/user/friendRequests", "id", "friendRequests");

        PO_PrivateView.checkUser(driver, "Mateo");
        PO_PrivateView.checkUser(driver, "Mateo");
    }

    //Sobre el listado de invitaciones recibidas. Hacer clic en el botón/enlace de una de ellas y
    //comprobar que dicha solicitud desaparece del listado de invitaciones.
    @Test
    @Order(22)
    public void PR22() {
        //primera peticion
        PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
        PO_LoginView.fillLoginForm(driver, "borrar@email.com", "borrar");
        By boton = By.id("btnPeticion");
        driver.findElement(boton).click();
        PO_HomeView.clickOption(driver, "logout", "class", "btn btn-primary");
        //segunda peticion
        PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
        PO_LoginView.fillLoginForm(driver, "borrar@email.com", "borrar");
        By boton2 = By.id("btnPeticion");
        driver.findElement(boton2).click();
        PO_HomeView.clickOption(driver, "logout", "class", "btn btn-primary");

        //prueba
        //Vamos al formulario de logueo.
        PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
        //Rellenamos el formulario
        PO_LoginView.fillLoginForm(driver, "test5@email.com", "test5");

        //vamos al apartado de friendRequests
        PO_HomeView.clickOption(driver, "/user/friendRequests", "id", "friendRequests");
        //user09 tiene 2 invitaciones
        PO_FriendRequestView.checkNumberOfFriendRequest(driver,2);
        //Aceptamos una de ellas
        List<WebElement> result = PO_View.checkElementBy(driver, "id", "btnAceptar");
        result.get(0).click();
        //Comprobamos que ahora solo hay una
        driver.navigate().refresh();
        PO_FriendRequestView.checkNumberOfFriendRequest(driver,1);
    }

    // Mostrar el listado de amigos de un usuario. Comprobar que el listado contiene los amigos que deben ser.
    @Test
    @Order(23)
    public void PR23() {
        // User09 en este punto tendra un amigo user11
        PO_FriendsView.goToFriendsList(driver, "test5@email.com", "test5");
        //Encontramos el usuario user11
        PO_PrivateView.checkUser(driver, "test24@email.com");
    }

    // Agregar Post Bien
    @Test
    @Order(24)
    public void PR24() {
        //Vamos al formulario de logueo.
        PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
        //Rellenamos el formulario
        PO_LoginView.fillLoginForm(driver, "test24@email.com", "test24");


        List<WebElement> elements3 =  PO_View.checkElementBy(driver, "free",  "//li[contains(@id,'addPosts')]");
        elements3.get(0).click();
        //rellenamos el formulario de post
        PO_PostView.fillForm(driver, "test24", "test24");

        //vamos a mis post y los contamos
        List<WebElement> elements1 =  PO_View.checkElementBy(driver, "free",  "//li[contains(@id,'myPosts')]");
        elements1.get(0).click();
        List<WebElement> myPosts = SeleniumUtils.waitLoadElementsBy(driver, "free", "//div//div/a", PO_View.getTimeout());
        int sizeOriginal = myPosts.size();

        //vamos a la pagina de añadir post
        List<WebElement> elements2 =  PO_View.checkElementBy(driver, "free",  "//li[contains(@id,'addPosts')]");
        elements2.get(0).click();
        //rellenamos el formulario de post
        PO_PostView.fillForm(driver, "test24", "test24");
        //vamos a la pagina con las publicaciones

        //vamos a mis post y los contamos
        List<WebElement> elements4 =  PO_View.checkElementBy(driver, "free",  "//li[contains(@id,'myPosts')]");
        elements4.get(0).click();
        List<WebElement> myPosts2 = SeleniumUtils.waitLoadElementsBy(driver, "free", "//div//div/a", PO_View.getTimeout());
        int sizeDespues = myPosts2.size();


        //Comprobamos q hay uno mas
        Assertions.assertEquals(sizeOriginal + 1, sizeDespues);

    }

    // Agregar Campo vacio
    @Test
    @Order(25)
    public void PR25() {


        //Vamos al formulario de logueo.
        PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
        //Rellenamos el formulario
        PO_LoginView.fillLoginForm(driver, "test24@email.com", "test24");

        //vamos a mis post y los contamos
        List<WebElement> elements1 =  PO_View.checkElementBy(driver, "free",  "//li[contains(@id,'myPosts')]");
        elements1.get(0).click();
        List<WebElement> myPosts = SeleniumUtils.waitLoadElementsBy(driver, "free", "//div//div/a", PO_View.getTimeout());
        int sizeOriginal = myPosts.size();
        driver.navigate().to(URL + "");
        //vamos a la pagina de añadir post
        List<WebElement> elements2 =  PO_View.checkElementBy(driver, "free",  "//li[contains(@id,'addPosts')]");
        elements2.get(0).click();
        //rellenamos el formulario de post
        PO_PostView.fillForm(driver, "", "test24");
        //vamos a la pagina con las publicaciones

        //vamos a mis post y los contamos
        List<WebElement> elements3 =  PO_View.checkElementBy(driver, "free",  "//li[contains(@id,'myPosts')]");
        elements3.get(0).click();
        List<WebElement> myPosts2 = SeleniumUtils.waitLoadElementsBy(driver, "free", "//div//div/a", PO_View.getTimeout());
        int sizeDespues = myPosts2.size();


        //Comprobamos q no se añadio ningun post
        Assertions.assertEquals(sizeOriginal , sizeDespues);


    }
    @Test
    @Order(26)
    public void PR26() {
        //Vamos al formulario de logueo.
        PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
        //Rellenamos el formulario
        PO_LoginView.fillLoginForm(driver, "test24@email.com", "test24");


        //vamos a mis post y los contamos
        List<WebElement> elements3 =  PO_View.checkElementBy(driver, "free",  "//li[contains(@id,'myPosts')]");
        elements3.get(0).click();
        List<WebElement> myPosts2 = SeleniumUtils.waitLoadElementsBy(driver, "free", "//div//div/a", PO_View.getTimeout());
        int sizeDespues = myPosts2.size();


        //Comprobamos q no se añadio ningun post
        Assertions.assertEquals(2 , sizeDespues);

    }




    @Test
    @Order(27)
    public void PR27() {
        //Vamos al formulario de logueo.
        PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
        //Rellenamos el formulario
        PO_LoginView.fillLoginForm(driver, "test5@email.com", "test5");


        //vamos a mis post y los contamos
        List<WebElement> elements3 =  PO_View.checkElementBy(driver, "free",  "//li[contains(@id,'friends')]");
        elements3.get(0).click();


        PO_View.checkElementBy(driver,"id","linkNombre").get(0).click();
        List<WebElement> myPosts2 = SeleniumUtils.waitLoadElementsBy(driver, "free", "//div//div/a", PO_View.getTimeout());
        int sizeDespues = myPosts2.size();
        //Comprobamos q no se añadio ningun post
        Assertions.assertEquals(2 , sizeDespues);

    }

    @Test
    @Order(28)
    public void PR28() {
        //Vamos al formulario de logueo.
        PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
        //Rellenamos el formulario
        PO_LoginView.fillLoginForm(driver, "test5@email.com", "test5");

        driver.navigate().to(URL + "/posts/borrar@email.com");//tiene que existir el usuario



        //Comprobamos que nos devuelve a la vista cuando no es amigo
       String checkText = "Post no disponibles";
        List<WebElement> result = PO_View.checkElementBy(driver, "id",  "postNoDisponibles");
        Assertions.assertEquals(checkText, result.get(0).getText());


    }


    @Test
    @Order(29)
    public void PR29() {
        //Escribimos la url en la página sin estar autenticados para listar usuarios
        driver.navigate().to("http://localhost:8081/admin/users");
        //Comprobamos que estamos en el formulario de login
        String checkText = "Identificación de usuario";
        List<WebElement> result = PO_View.checkElementBy(driver, "text", checkText);
        Assertions.assertEquals(checkText, result.get(0).getText());

    }

    @Test
    @Order(30)
    public void PR30() {
        //Escribimos la url en la página sin estar autenticados para listar invitaciones de amistad
        driver.navigate().to("http://localhost:8081/user/friendRequests");
        //Comprobamos que estamos en el formulario de login
        String checkText = "Identificación de usuario";
        List<WebElement> result = PO_View.checkElementBy(driver, "text", checkText);
        Assertions.assertEquals(checkText, result.get(0).getText());

    }

    @Test
    @Order(31)
    public void PR31() {
        /*
            Este punto no hemos podido comprobarlo ya que un usuario tan solo puede acceder a su propio listado de amigos.
            La ruta que muestra los amigos de un usuario es /user/friends y no /user/friends/:id. El listado se muestra siempre
            de los amigos del usuario en sesión por lo que no podrá acceder ningún usuario a un listado que no sea el suyo.

            Como comprobación adicional, probaremos que un usuario no logeado no puede acceder al listado de amigos
         */
        driver.navigate().to("http://localhost:8081/user/friends");
        //Comprobamos que estamos en el formulario de login
        String checkText = "Identificación de usuario";
        List<WebElement> result = PO_View.checkElementBy(driver, "text", checkText);
        Assertions.assertEquals(checkText, result.get(0).getText());

    }





    //PR33. Inicio de sesión con datos válidos
    @Test
    @Order(32)
    void PR32() {
        registrarUser("api@email.com","api");
        registrarUser("api1@email.com","api1");
        driver.navigate().to("http:localhost:8081/apiclient/client.html");
        //Rellenamos el formulario
        PO_LoginView.fillLoginForm(driver, "api@email.com", "api");


    }

    //PR34. Inicio de sesión con datos inválidos (usuario no existente en la aplicación).
    @Test
    @Order(33)
    void PR33() {
        driver.navigate().to("http:localhost:8081/apiclient/client.html");
        //Rellenamos el formulario
        PO_LoginView.fillLoginForm(driver, "api@email.com", "mal");
    }

    @Test
    @Order(34)
    void PR34() {
        driver.manage().deleteAllCookies();
        driver.navigate().to("http://localhost:8081/apiclient/client.html");
        //Rellenamos el formulario
        WebDriverWait wait = new WebDriverWait(driver, 10);
        WebElement element = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        PO_LoginView.fillLoginForm(driver, "api@email.com", "api");
        //Al loguearnos nos deben salir los textos de la tabla de nuestros amigos
        //Esperamos un poco a que cargue la página
        wait = new WebDriverWait(driver, 10);
        element = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("friendEmail")));
        SeleniumUtils.textIsPresentOnPage(driver, "Email");
        SeleniumUtils.textIsPresentOnPage(driver, "Nombre");
        SeleniumUtils.textIsPresentOnPage(driver, "Apellidos");


        List<WebElement> elements = driver.findElements(By.id("friendEmail"));
        Assertions.assertTrue( elements.size() > 3);
    }

    @Test
    @Order(36)
    void PR36() {

        driver.navigate().to("http:localhost:8081/apiclient/client.html");
        //Rellenamos el formulario
        PO_LoginView.fillLoginForm(driver, "api1@email.com", "api1");

        //Accedemos al chat de un usuario amigo y enviamos un mensaje
        WebElement chat = driver.findElement(By.id("chat"));
        chat.click();
        //Enviamos un mensaje al usuario
        WebElement textField = driver.findElement(By.name("text-add"));
        textField.click();
        textField.clear();
        textField.sendKeys("Hola que tal");
        //Le damos a boton de enviar
        WebElement send = driver.findElement(By.id("boton-add"));
        send.click();
        //Comprobamos que aparece el nuevo mensaje

        SeleniumUtils.textIsPresentOnPage(driver, "Hola que tal");
        //Cerramos sesion
    }

    @Test
    @Order(37)
    void PR37() {


        driver.navigate().to("http:localhost:8081/apiclient/client.html");
        //Rellenamos el formulario
        PO_LoginView.fillLoginForm(driver, "api1@email.com", "api1");
        //Al loguearnos nos deben salir los textos de la tabla de nuestros amigos
        //Esperamos un poco a que cargue la página
        //Accedemos al chat de un usuario amigo y enviamos un mensaje
        WebElement chat = driver.findElement(By.id("chat"));
        chat.click();
        //Enviamos un mensaje al usuario
        WebElement textField = driver.findElement(By.name("text-add"));
        textField.click();
        textField.clear();
        textField.sendKeys("Hola que tal");
        //Le damos a boton de enviar
        WebElement send = driver.findElement(By.id("boton-add"));
        send.click();
        //Esperamos a que se carguen los mensajes
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);


        driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
        SeleniumUtils.textIsPresentOnPage(driver, "Hola que tal");

        driver.manage().deleteAllCookies();
        driver.navigate().to("http:localhost:8081/apiclient/client.html?w=login");
        PO_LoginView.fillLoginForm(driver, "api@email.com", "api");
        //Al loguearnos nos deben salir los textos de la tabla de nuestros amigos
        WebElement chat1 = driver.findElement(By.id("chat"));
        chat1.click();
        SeleniumUtils.textIsPresentOnPage(driver, "Hola que tal");
        //Esperamos un poco a que cargue la página

    }
    @Test
    @Order(40)
    public void PR40() {

        //Se entra en la aplicación con rol de Administrador
        PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
        PO_LoginView.fillLoginForm(driver, "admin@email.com", "admin");

        // Se selecciona el último checkBox
        List<Integer> positions = new ArrayList<>();
        positions.add(0);
        positions.add(1);
        positions.add(2);
        PO_AdminUsersListView.markCheckBoxElements(driver,positions);
        // Se pulsa el botón eliminar
        PO_AdminUsersListView.clickBtn(driver);



    }
}
