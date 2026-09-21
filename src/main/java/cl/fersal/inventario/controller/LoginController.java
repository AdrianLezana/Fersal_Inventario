package cl.fersal.inventario.controller;

import cl.fersal.inventario.dao.UsuarioDAO;
import cl.fersal.inventario.model.Usuario;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Label lblMensaje;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @FXML
    private void iniciarSesion(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            lblMensaje.setStyle("-fx-text-fill: red;");
            lblMensaje.setText("Por favor, ingrese sus credenciales.");
            return;
        }

        Usuario usuario = usuarioDAO.obtenerPorUsername(username);

        // NOTA: Para esta primera fase compararemos la contraseña plana.
        // A futuro implementaremos un algoritmo de Hash (ej. BCrypt) por seguridad.
        if (usuario != null && usuario.getPasswordHash().equals(password)) {
            lblMensaje.setStyle("-fx-text-fill: green;");
            lblMensaje.setText("¡Bienvenido, " + usuario.getUsername() + "!");

            try {
                cl.fersal.inventario.app.App.cambiarEscena("/cl/fersal/inventario/fxml/dashboard.fxml", "Fersal - Panel de Inventario", 900, 600);
            } catch (java.io.IOException e) {
                lblMensaje.setStyle("-fx-text-fill: red;");
                lblMensaje.setText("Error al cargar el panel principal.");
                System.err.println(e.getMessage());
            }
        } else {
            lblMensaje.setStyle("-fx-text-fill: red;");
            lblMensaje.setText("Usuario o contraseña incorrectos.");
        }
    }
}