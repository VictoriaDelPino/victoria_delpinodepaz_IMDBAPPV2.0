package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Remote;

import android.app.Activity;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

public class FirebaseAuthManager {
    public static void register(Activity context, String email, String password) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Registro exitoso, puede iniciar sesión", Toast.LENGTH_SHORT).show();
                        } else {
                            // Manejar errores específicos
                            if (task.getException() instanceof FirebaseAuthException) {
                                FirebaseAuthException authException = (FirebaseAuthException) task.getException();
                                if (authException.getErrorCode().equals("ERROR_EMAIL_ALREADY_IN_USE")) {
                                    Toast.makeText(context, "Este email ya está registrado.", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(context, "Error: " + authException.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(context, "Error inesperado: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    public static void login(Activity context, String email, String password, OnLoginListener listener) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            listener.onSuccess();
                        } else {
                            listener.onFailure(task.getException().getMessage());
                        }
                    }
                });
    }

    // Interfaz para manejar el resultado del login
    public interface OnLoginListener {
        void onSuccess();
        void onFailure(String errorMessage);
    }

}
