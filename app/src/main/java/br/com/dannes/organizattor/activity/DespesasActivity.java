package br.com.dannes.organizattor.activity;

//import android.support.design.widget.TextInputEditText;

import com.google.android.material.textfield.TextInputEditText;
//import com.google.android.material.textfield.TextInputLayout;
//import android.support.v7.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class DespesasActivity extends AppCompatActivity {

    private TextInputEditText campoData, campoCategoria, campoDescricao;
    private EditText campoValor;
    public Movimentacao movimentacao;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private Double despesaTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_despesas);

        campoValor = findViewById(R.id.editValor);
        campoData = findViewById(R.id.editData);
        campoCategoria = findViewById(R.id.editCategoria);
        campoDescricao = findViewById(R.id.editDescricao);

        //Preenche o campo data com a date atual
        campoData.setText( DateCustom.dataAtual() );
        recuperarDespesaTotal();

    }

    public void salvarDespesa(View view){

        if ( validarCamposDespesa() ){

            movimentacao = new Movimentacao();
            String data = Objects.requireNonNull(campoData.getText()).toString();
            Double valorRecuperado;
            valorRecuperado = Double.parseDouble(campoValor.getText().toString());

            movimentacao.setValor( valorRecuperado );
            movimentacao.setCategoria( Objects.requireNonNull(campoCategoria.getText()).toString() );
            movimentacao.setDescricao( Objects.requireNonNull(campoDescricao.getText()).toString() );
            movimentacao.setData( data );
            movimentacao.setTipo( "d" );

            Double despesaAtualizada = despesaTotal + valorRecuperado;
            atualizarDespesa( despesaAtualizada );

            movimentacao.salvar( data );

            finish();

        }


    }

    public Boolean validarCamposDespesa(){

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
                        Toast.makeText(DespesasActivity.this,
                                "Descrição não foi preenchida!",
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }else {
                    Toast.makeText(DespesasActivity.this,
                            "Categoria não foi preenchida!",
                            Toast.LENGTH_SHORT).show();
                    return false;
                }
            }else {
                Toast.makeText(DespesasActivity.this,
                        "Data não foi preenchida!",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }else {
            Toast.makeText(DespesasActivity.this,
                    "Valor não foi preenchido!",
                    Toast.LENGTH_SHORT).show();
            return false;
        }


    }

    public void recuperarDespesaTotal(){

        String emailUsuario = Objects.requireNonNull(autenticacao.getCurrentUser()).getEmail();
        assert emailUsuario != null;
        String idUsuario = Base64Custom.codificarBase64( emailUsuario );
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child( idUsuario );

        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue( Usuario.class );
                assert usuario != null;
                despesaTotal = usuario.getDespesaTotal();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void atualizarDespesa(Double despesa){

        String emailUsuario = Objects.requireNonNull(autenticacao.getCurrentUser()).getEmail();
        assert emailUsuario != null;
        String idUsuario = Base64Custom.codificarBase64( emailUsuario );
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child( idUsuario );

        usuarioRef.child("despesaTotal").setValue(despesa);

    }

}
