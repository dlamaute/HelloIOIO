package ioio.examples.hello;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.AbstractIOIOActivity;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ToggleButton;

/**
 * This is the main activity of the HelloIOIO example application.
 * 
 * It displays a toggle button on the screen, which enables control of the
 * on-board LED. This example shows a very simple usage of the IOIO, by using
 * the {@link IOIOActivity} class. For a more advanced use case, see the
 * HelloIOIOPower example.
 */
@SuppressWarnings("deprecation")
public class MainActivity extends AbstractIOIOActivity{
	private ToggleButton button_;
	private final int LED1_PIN = 1;
	private final int LED2_PIN = 2;

	private Button mLed1Button, mLed2Button;
	
	private boolean mLed1State = false;
	private boolean mLed2State = false;

	/**
	 * Called when the activity is first created. Here we normally initialize
	 * our GUI.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		button_ = (ToggleButton) findViewById(R.id.button);
		mLed1Button = (Button) findViewById(R.id.btn1);
		mLed1Button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mLed1State == true) {
					mLed1State = false;
					mLed1Button.setText("Apagado");
				} else {
					mLed1State = true;
					mLed1Button.setText("Prendido");
				}
			}
		});
		mLed2Button = (Button) findViewById(R.id.btn2);
		mLed2Button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mLed2State = false;
				mLed2State = true;
				mLed2Button.setText("Relay On");
				
				//wait for time t and then run
				final Handler handler = new Handler();
				handler.postDelayed(new Runnable() { 
				   @Override
				   public void run() {
					   // on launch of activity we execute an async task 
					   mLed2State = false;
					   mLed2Button.setText("Relay Off");  
				
				   }
				}, 2000);
			}
		});
	}

	/**
	 * This is the thread on which all the IOIO activity happens. It will be run
	 * every time the application is resumed and aborted when it is paused. The
	 * method setup() will be called right after a connection with the IOIO has
	 * been established (which might happen several times!). Then, loop() will
	 * be called repetitively until the IOIO gets disconnected.
	 */
	class IOIOThread extends AbstractIOIOActivity.IOIOThread {
		/** The on-board LED. */
		private DigitalOutput led_;
		private DigitalOutput power;
		private DigitalOutput power2;

		/**
		 * Called every time a connection with IOIO has been established.
		 * Typically used to open pins.
		 * 
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * 
		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#setup()
		 */
		@Override
		protected void setup() throws ConnectionLostException {
			led_ = ioio_.openDigitalOutput(0, true);
			power = ioio_.openDigitalOutput(LED1_PIN, false);
			power2 = ioio_.openDigitalOutput(LED2_PIN, false);
		}

		/**
		 * Called repetitively while the IOIO is connected.
		 * 
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * 
		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#loop()
		 */
		@Override
		public void loop() throws ConnectionLostException {
			led_.write(!button_.isChecked());
			power.write(mLed1State);
			power2.write(mLed2State);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}

	/**
	 * A method to create our IOIO thread.
	 * 
	 * @see ioio.lib.util.AbstractIOIOActivity#createIOIOThread()
	 */
	@Override
	protected AbstractIOIOActivity.IOIOThread createIOIOThread() {
		return new IOIOThread();
	}
}