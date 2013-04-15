package ioio.examples.hello;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
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
public class MainActivity extends IOIOActivity implements OnSeekBarChangeListener{
	private ToggleButton button_;
	private final int LED1_PIN = 1;
	private final int LED2_PIN = 2;
	
	private final int Heat_Pin = 34;
	private final int PWM_FREQ = 10000;
	private final int Polling_Delay = 150;
	private SeekBar Seekbar;
	private int HeatState;
	private long LastChange;

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
		
		Seekbar = (SeekBar) findViewById(R.id.SeekBar);
		Seekbar.setOnSeekBarChangeListener(this);
		Seekbar.setProgress(HeatState);
		
		enableUi(false);
	}

	/**
	 * This is the thread on which all the IOIO activity happens. It will be run
	 * every time the application is resumed and aborted when it is paused. The
	 * method setup() will be called right after a connection with the IOIO has
	 * been established (which might happen several times!). Then, loop() will
	 * be called repetitively until the IOIO gets disconnected.
	 */
	class Looper extends BaseIOIOLooper {
		/** The on-board LED. */
		private DigitalOutput led_;
		private DigitalOutput power;
		private DigitalOutput power2;
		private PwmOutput Heater;

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
			try{
				led_ = ioio_.openDigitalOutput(0, true);
				power = ioio_.openDigitalOutput(LED1_PIN, false);
				power2 = ioio_.openDigitalOutput(LED2_PIN, false);
				Heater = ioio_.openPwmOutput(Heat_Pin, PWM_FREQ);
				enableUi(true);
			}catch(ConnectionLostException e){
				enableUi(false);
				throw e;
			}
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
			try {
				led_.write(!button_.isChecked());
				power.write(mLed1State);
				power2.write(mLed2State);
				Heater.setPulseWidth(HeatState);
				Thread.sleep(100);
			} catch (InterruptedException e) {
				enableUi(false);
				e.printStackTrace(); 
			}
		}
	}

	/**
	 * A method to create our IOIO thread.
	 * 
	 * @see ioio.lib.util.AbstractIOIOActivity#createIOIOThread()
	 */
	@Override
	protected IOIOLooper createIOIOLooper() {
        return new Looper();
    }
	
	private void enableUi(final boolean enable){
		runOnUiThread(new Runnable(){
			@Override
			public void run(){
				Seekbar.setEnabled(enable);
			}
		});
	}
	
	@Override
	public void onProgressChanged(SeekBar seedBar, int progress, boolean fromUser){
		if(System.currentTimeMillis() - LastChange > Polling_Delay){
			updateState(Seekbar);
			LastChange = System.currentTimeMillis();
		}
	}
	
	@Override
	public void onStartTrackingTouch(SeekBar seekbar){
		LastChange = System.currentTimeMillis();
	}
	
	@Override
	public void onStopTrackingTouch(SeekBar seekbar){
		updateState(seekbar);
	}
	
	private void updateState(final SeekBar seekbar){
		HeatState = seekbar.getProgress();
	}
}