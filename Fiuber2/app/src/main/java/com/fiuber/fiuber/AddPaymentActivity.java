package com.fiuber.fiuber;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.braintreepayments.cardform.OnCardFormSubmitListener;
import com.braintreepayments.cardform.utils.CardType;
import com.braintreepayments.cardform.view.CardEditText;
import com.braintreepayments.cardform.view.CardForm;
import com.braintreepayments.cardform.view.SupportedCardTypesView;
import com.fiuber.fiuber.server.ServerHandler;

public class AddPaymentActivity extends AppCompatActivity implements OnCardFormSubmitListener,
        CardEditText.OnCardTypeChangedListener {
    private static final String TAG = "AddPaymentActivity";

    SharedPreferences mPreferences;

    private static final CardType[] SUPPORTED_CARD_TYPES = { CardType.VISA, CardType.MASTERCARD,
                                                             CardType.AMEX };

    private SupportedCardTypesView mSupportedCardTypesView;

    protected CardForm mCardForm;

    private String mCardType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.card_form);

        mPreferences = getSharedPreferences(Constants.MY_PREFERENCES, Context.MODE_PRIVATE);

        Log.d(TAG, "Here1");
        mSupportedCardTypesView = findViewById(R.id.supported_card_types);
        mSupportedCardTypesView.setSupportedCardTypes(SUPPORTED_CARD_TYPES);

        Log.d(TAG, "Here2");
        mCardForm = findViewById(R.id.card_form);
        mCardForm.cardRequired(true)
                .expirationRequired(true)
                .cvvRequired(true)
                .setup(this);
        mCardForm.setOnCardFormSubmitListener(this);
        mCardForm.setOnCardTypeChangedListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();    //Call the back button's method
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String generateCardType(String numberString){
       if (numberString.startsWith("4"))
           return "VISA";
       if (numberString.startsWith("4"))
           return "AMEX";
        if (numberString.startsWith("5"))
            return "MASTERCARD";
        return "";
    }

    @Override
    public void onCardFormSubmit() {
        if (mCardForm.isValid()) {
            Toast.makeText(this, R.string.valid, Toast.LENGTH_SHORT).show();
            String expirationMonth = mCardForm.getExpirationMonth();
            String expirationYear =  mCardForm.getExpirationYear();
            String number = mCardForm.getCardNumber();
            String cvv = mCardForm.getCvv();

            mPreferences.edit().putString(Constants.KEY_EXPIRATION_MONTH, expirationMonth);
            mPreferences.edit().putString(Constants.KEY_EXPIRATION_YEAR, expirationYear);
            mPreferences.edit().putString(Constants.KEY_METHOD, "card");
            mPreferences.edit().putString(Constants.KEY_NUMBER, number);
            mPreferences.edit().putString(Constants.KEY_CVV, cvv);
            mPreferences.edit().putString(Constants.KEY_PAYMENT_TYPE, mCardType);

            finish();

        } else {
            mCardForm.validate();
            Toast.makeText(this, R.string.invalid, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCardTypeChanged(CardType cardType) {
        if (cardType == CardType.EMPTY) {
            mSupportedCardTypesView.setSupportedCardTypes(SUPPORTED_CARD_TYPES);

        } else {
            mSupportedCardTypesView.setSelected(cardType);
            mCardType = cardType.toString();
        }
    }
}