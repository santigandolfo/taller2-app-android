package com.fiuber.fiuber;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.braintreepayments.cardform.OnCardFormSubmitListener;
import com.braintreepayments.cardform.utils.CardType;
import com.braintreepayments.cardform.view.CardEditText;
import com.braintreepayments.cardform.view.CardForm;
import com.braintreepayments.cardform.view.SupportedCardTypesView;

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_sign_out) {
            onBackPressed();
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.card_form);

        mPreferences = getSharedPreferences(Constants.KEY_MY_PREFERENCES, Context.MODE_PRIVATE);

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
    public void onCardFormSubmit() {
        if (mCardForm.isValid()) {
            Toast.makeText(this, R.string.valid, Toast.LENGTH_SHORT).show();
            String expirationMonth = mCardForm.getExpirationMonth();
            String expirationYear =  mCardForm.getExpirationYear();
            String number = mCardForm.getCardNumber();
            String ccvv = mCardForm.getCvv();

            mPreferences.edit().putString(Constants.KEY_EXPIRATION_MONTH, expirationMonth).apply();
            mPreferences.edit().putString(Constants.KEY_EXPIRATION_YEAR, expirationYear).apply();
            mPreferences.edit().putString(Constants.KEY_METHOD, "card").apply();
            mPreferences.edit().putString(Constants.KEY_NUMBER, number).apply();
            mPreferences.edit().putString(Constants.KEY_CCVV, ccvv).apply();
            mPreferences.edit().putString(Constants.KEY_PAYMENT_TYPE, mCardType).apply();

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