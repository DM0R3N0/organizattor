package br.com.dannes.organizattor.activity;

//import android.support.design.widget.TextInputEditText;
import androidx.appcompat.app.AppCompatActivity;
//import android.support.v7.app.AppCompatActivity;
//import androidx.annotation.NonNull;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import br.com.dannes.organizattor.R;
import br.com.dannes.organizattor.config.ConfiguracaoFirebase;
import br.com.dannes.organizattor.helper.Base64Custom;
import br.com.dannes.organizattor.helper.DateCustom;
import br.com.dannes.organizattor.model.Movimentacao;
import br.com.dannes.organizattor.model.Usuario;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class ReceitasActivity extends AppCompatActivity {

    private TextInputEditText campoData, campoCategoria, campoDescricao;
    private EditText campoValor;
    public Movimentacao movimentacao;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private Double receitaTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receitas);

        campoValor = findViewById(R.id.editValor);
        campoData = findViewById(R.id.editData);
        campoCategoria = findViewById(R.id.editCategoria);
        campoDescricao = findViewById(R.id.editDescricao);

        //Preenche o campo data com a date atual
        campoData.setText( DateCustom.dataAtual() );
        recuperarReceitaTotal();

    }

    public void salvarReceita(View view){

        if ( validarCamposReceita() ){

            movimentacao = new Movimentacao();
            String data;
            data = Objects.requireNonNull(campoData.getText()).toString();
            Double valorRecuperado;
            valorRecuperado = Double.parseDouble(campoValor.getText().toString());

            movimentacao.setValor( valorRecuperado );
            movimentacao.setCategoria( Objects.requireNonNull(campoCategoria.getText()).toString() );
            movimentacao.setDescricao( Objects.requireNonNull(campoDescricao.getText()).toString() );
            movimentacao.setData( data );
            movimentacao.setTipo( "r" );

            Double receitaAtualizada = receitaTotal + valorRecuperado;
            atualizarReceita( receitaAtualizada );

            movimentacao.salvar( data );

            finish();

        }


    }

    public Boolean validarCamposReceita(){

        String textoValor;
        textoValor = campoValor.getText().toString();
        String textoData = Objects.requireNonNull(campoData.getText()).toString();
        String textoCategoria = Objects.requireNonNull(campoCategoria.getText()).toString();
        String textoDescricao = Objects.requireNonNull(campoDescricao.getText()).toString();

        if ( !textoValor.isEmpty() ){
            if ( !textoData.isEmpty() ){
                if ( !textoCategoria.isEmpty() ){
                    if ( !textoDescricao.isEmpty() ){
                        return true;
                    }else {
                        Toast.makeText(ReceitasActivity.this,
                                "Descrição não foi preenchida!",
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }else {
                    Toast.makeText(ReceitasActivity.this,
                            "Categoria não foi preenchida!",
                            Toast.LENGTH_SHORT).show();
                    return false;
                }
            }else {
                Toast.makeText(ReceitasActivity.this,
                        "Data não foi preenchida!",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }else {
            Toast.makeText(ReceitasActivity.this,
                    "Valor não foi preenchido!",
                    Toast.LENGTH_SHORT).show();
            return false;
        }


    }

    public void recuperarReceitaTotal(){

        String emailUsuario = Objects.requireNonNull(autenticacao.getCurrentUser()).getEmail();
        assert emailUsuario != null;
        String idUsuario = Base64Custom.codificarBase64( emailUsuario );
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child( idUsuario );

        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue( Usuario.class );
                assert usuario != null;
                receitaTotal = usuario.getReceitaTotal();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void atualizarReceita(Double receita){

        String emailUsuario = Objects.requireNonNull(autenticacao.getCurrentUser()).getEmail();
        assert emailUsuario != null;
        String idUsuario = Base64Custom.codificarBase64( emailUsuario );
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child( idUsuario );

        usuarioRef.child("receitaTotal").setValue(receita);

    }

}
