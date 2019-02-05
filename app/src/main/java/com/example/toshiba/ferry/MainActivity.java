package com.example.toshiba.ferry;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.example.toshiba.ferry.Model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import dmax.dialog.SpotsDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

	Button btnSignIn, btnReg;
	MaterialEditText edtEmail, edtPass, edtName, edtPhone;
	RelativeLayout rootLayout;

	FirebaseAuth auth;
	FirebaseDatabase db;
	DatabaseReference users;

	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*
		CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
		.setDefaultFontPath("fonts/Kosmodoggy.ttf")
		.setFontAttrId(R.attr.fontPath)
		.build());
		*/

		setContentView(R.layout.activity_main);

		auth = FirebaseAuth.getInstance();
		db = FirebaseDatabase.getInstance();
		users = db.getReference("Users");

		btnSignIn = (Button) findViewById(R.id.btnSignIn);
		btnReg = (Button) findViewById(R.id.btnRegister);
		rootLayout = (RelativeLayout)findViewById(R.id.rootLayout);
		
		btnSignIn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showSignInDialog();
			}
		});

		btnReg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showRegisterDialog();
			}
		});
	}

	private void showSignInDialog() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle("SIGN IN ");
		dialog.setMessage("Please use email to sign in");

		LayoutInflater inflater = LayoutInflater.from(this);
		View sign_in_layout = inflater.inflate(R.layout.layout_sign_in,null);

		edtEmail = sign_in_layout.findViewById(R.id.edtEmail);
		edtPass = sign_in_layout.findViewById(R.id.edtPassword);

		dialog.setView(sign_in_layout);

		dialog.setPositiveButton("SIGN IN", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

				btnSignIn.setEnabled(false);

				if(TextUtils.isEmpty(edtEmail.getText().toString()))
				{
					Snackbar.make(rootLayout,"Please enter email address", Snackbar.LENGTH_SHORT)
							.show();
					return;
				}
				if(TextUtils.isEmpty(edtPass.getText().toString()))
				{
					Snackbar.make(rootLayout,"Please enter your name", Snackbar.LENGTH_SHORT)
							.show();
					return;
				}
				if(edtPass.getText().toString().length() < 6)
				{
					Snackbar.make(rootLayout,"Password is too short!", Snackbar.LENGTH_SHORT)
							.show();
					return;
				}

				final SpotsDialog waitingDialog = new SpotsDialog(MainActivity.this);
				waitingDialog.show();

				auth.signInWithEmailAndPassword(edtEmail.getText().toString(), edtPass.getText().toString())
						.addOnSuccessListener(new OnSuccessListener<AuthResult>() {
							@Override
							public void onSuccess(AuthResult authResult) {
								waitingDialog.dismiss();
								startActivity(new Intent(MainActivity.this, Home.class));
								finish();
							}
						}).addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						waitingDialog.dismiss();
						Snackbar.make(rootLayout,"Failed " +e.getMessage(), Snackbar.LENGTH_SHORT)
								.show();

						btnSignIn.setEnabled(true);
					}
				});
			}
		});

		dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	private void showRegisterDialog() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle("REGISTER ");
		dialog.setMessage("Please use email to register");

		LayoutInflater inflater = LayoutInflater.from(this);
		View register_layout = inflater.inflate(R.layout.layout_register,null);

		edtEmail = register_layout.findViewById(R.id.edtEmail);
		edtPass = register_layout.findViewById(R.id.edtPassword);
		edtName = register_layout.findViewById(R.id.edtName);
		edtPhone = register_layout.findViewById(R.id.edtPhone);

		dialog.setView(register_layout);

		dialog.setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

				if(TextUtils.isEmpty(edtEmail.getText().toString()))
				{
					Snackbar.make(rootLayout,"Please enter email address", Snackbar.LENGTH_SHORT)
							.show();
					return;
				}
				if(TextUtils.isEmpty(edtName.getText().toString()))
				{
					Snackbar.make(rootLayout,"Please enter your name", Snackbar.LENGTH_SHORT)
							.show();
					return;
				}
				if(TextUtils.isEmpty(edtPhone.getText().toString()))
				{
					Snackbar.make(rootLayout,"Please enter phone number", Snackbar.LENGTH_SHORT)
							.show();
					return;
				}
				if(TextUtils.isEmpty(edtPass.getText().toString()))
				{
					Snackbar.make(rootLayout,"Please enter your password", Snackbar.LENGTH_SHORT)
							.show();
					return;
				}
				if(edtPass.getText().toString().length() < 6)
				{
					Snackbar.make(rootLayout,"Password is too short!", Snackbar.LENGTH_SHORT)
							.show();
					return;
				}

				SpotsDialog waitingDialog = new SpotsDialog(MainActivity.this);
				waitingDialog.show();

				auth.createUserWithEmailAndPassword(edtEmail.getText().toString(),edtPass.getText().toString())
						.addOnSuccessListener(new OnSuccessListener<AuthResult>() {
							@Override
							public void onSuccess(AuthResult authResult) {
								User user = new User();
								user.setEmail(edtEmail.getText().toString());
								user.setName(edtName.getText().toString());
								user.setPhone(edtPhone.getText().toString());
								user.setPassword(edtPass.getText().toString());

								users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
										.setValue(user)
										.addOnSuccessListener(new OnSuccessListener<Void>() {
											@Override
											public void onSuccess(Void aVoid) {
												Snackbar.make(rootLayout, "Successfully Registered!", Snackbar.LENGTH_SHORT)
														.show();

											}
										})
										.addOnFailureListener(new OnFailureListener() {
											@Override
											public void onFailure(@NonNull Exception e) {
												Snackbar.make(rootLayout,"Failed " + e.getMessage(), Snackbar.LENGTH_SHORT)
														.show();
											}
										});
							}
						})
				.addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						Snackbar.make(rootLayout,"Failed " + e.getMessage(), Snackbar.LENGTH_SHORT)
								.show();
					}
				});
			}
		});

		dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		dialog.show();
	}
}
