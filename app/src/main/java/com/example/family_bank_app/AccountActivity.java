package com.example.family_bank_app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.lang.String.format;
import static java.lang.String.valueOf;

public class AccountActivity extends AppCompatActivity implements Dialog_DepositWithdraw.DepositWithdrawDialogListener {
    RecyclerView transactionRecyclerView;
    MyTransactionAdapter myTransactionAdapter;
    List<String> transactionName, date;
    List<Double> amount, currentBal;
    List<Long> UIDS;
    String name;
    Double balance;

    AccountViewModel accountViewModel;
    TransactionViewModel transactionViewModel;
    TextView accountName, accountBal;

    //Call df.format(DOUBLE) to output a string with proper formatting
    DecimalFormat df = new DecimalFormat("0.00");

    //inits for deposit and withdraw dialog
    EditText deposit_withdraw_dialog;
    Button btn_withdraw, btn_deposit;
    private int status_depositWithdraw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        transactionName = new ArrayList<String>();
        amount = new ArrayList<Double>();
        currentBal = new ArrayList<Double>();
        UIDS = new ArrayList<Long>();
        name = "";
        balance = 0.0;


        accountViewModel = new AccountViewModel();

        int pos = getIntent().getIntExtra("POSITION", 0);
        Long UID = getIntent().getLongExtra("UID", 0);

        accountName = findViewById(R.id.NameOfAccount);
        accountBal = findViewById(R.id.balance);

        transactionRecyclerView = findViewById(R.id.TransactionRecycler);

        myTransactionAdapter = new MyTransactionAdapter(this, transactionName , amount, currentBal, date, UIDS);
        transactionRecyclerView.setAdapter(myTransactionAdapter);
        transactionRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        myTransactionAdapter.notifyDataSetChanged();


        final Observer<AccountEntity> getAccountObserver = Account -> {
            if (Account == null) {
                return;
            }
            name = Account.getAccountName();
            balance = Account.getAccountBalance();

            StringBuilder builderBalance = new StringBuilder();
            builderBalance.append("Balance: $");
            builderBalance.append(df.format(balance));

            accountName.setText(name);
            accountBal.setText(builderBalance);
        };

        AccountViewModel.getAccount(this, UID).observe(this, getAccountObserver);


        final Observer<List<TransactionEntity>> getTransactionsObserver = transactionEntities -> {
            if (transactionEntities == null || transactionEntities.size() < 0) {
                return;
            }

            transactionName.clear();
            currentBal.clear();
            amount.clear();
            UIDS.clear();
//            date.clear();

            for(int i=0; i < transactionEntities.size();i++) {
                TransactionEntity transaction = transactionEntities.get(i);
                transactionName.add(transaction.getTransactionTitle());
                currentBal.add(transaction.getTransactionAmount());
                amount.add(transaction.getTransactionAmount());
                UIDS.add(transaction.getTransactionUid());
                date.add(transaction.getTransactionDate());
            }

            myTransactionAdapter.notifyDataSetChanged();
        };

        TransactionViewModel.getAllTransactions(this, UID).observe(this, getTransactionsObserver);

        /*
        Code for deposit and withdraw dialog below:
        */
        btn_deposit = findViewById(R.id.Btn_Deposit);
        btn_deposit.setOnClickListener(new View.OnClickListener(){
            public void onClick (View v){
                //CLICK DEPOSIT
                //Toast.makeText(getApplicationContext(), "click deposit", Toast.LENGTH_LONG).show();
                status_depositWithdraw = Dialog_DepositWithdraw.STATUS_DEPOSIT;
                depositWithdrawDialog();
            }
        });

        btn_withdraw = findViewById(R.id.Btn_Withdraw);
        btn_withdraw.setOnClickListener(new View.OnClickListener(){
            public void onClick (View v){
                //CLICK WITHDRAW
                //Toast.makeText(getApplicationContext(), "click withdraw", Toast.LENGTH_LONG).show();
                status_depositWithdraw = Dialog_DepositWithdraw.STATUS_WITHDRAW;
                depositWithdrawDialog();
            }
        });
    }

    //Called when either deposit or withdraw is clicked
    public void depositWithdrawDialog() {
        Dialog_DepositWithdraw depwithDialog = new Dialog_DepositWithdraw();
        depwithDialog.show(getSupportFragmentManager(), "deposit and withdraw dialog");
    }

    @Override
    public void sendText(double amount, String memo) {
        //truncate value to two decimal places
        DecimalFormat truncate = new DecimalFormat("#.##");
        truncate.setRoundingMode(RoundingMode.DOWN); //Throw away any entered decimal places past two
        double formatAmount = Double.parseDouble(truncate.format(amount));


        if (status_depositWithdraw == Dialog_DepositWithdraw.STATUS_WITHDRAW) {
            formatAmount = formatAmount * -1;
        }
        //Right now takes in a double dollar amount and string memo
        //Then toasts out an int cent value to change and the memo
        Toast.makeText(getApplicationContext(), "" + formatAmount + " " + memo, Toast.LENGTH_LONG).show();
        //send transaction to Transaction handler
    }
}