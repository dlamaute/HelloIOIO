package ioio.examples.hello;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.DigitalOutput.Spec.Mode;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * This is the main activity of the HelloIOIO example application.
 * 
 * It displays a toggle button on the screen, which enables control of the
 * on-board LED. This example shows a very simple usage of the IOIO, by using
 * the {@link IOIOActivity} class. For a more advanced use case, see the
 * HelloIOIOPower example.
 */

public class MainActivity extends IOIOActivity implements OnClickListener, OnSeekBarChangeListener{
	private final int LED_PIN = 0;
	private ToggleButton LED_Btn;
	private boolean LED_State = true;
	
	private final int Lamp_PIN = 1;
	private ToggleButton Lamp_Btn;
	private boolean Lamp_State = false;
	
	private final int Heat_PIN = 2;
	private SeekBar Heat_Bar;
	private int Heat_State;
	private final int Heat_FREQ = 100;
	private TextView PWM;
	float pulse;
	private Handler pulseHandler; 
	
	private final int Smell_PIN = 3;
	private ToggleButton Smell_Btn;
	private boolean Smell_State = false;
	
	private final int Analog_PIN = 40;
	private SeekBar Analog_Bar;
	private TextView analog_Value;
	private final int Analog_LED_PIN = 4;
	float Analog_Reading = 0;
	
	private final int Vibrator_PIN = 5;
	private SeekBar Vibrator_Bar;
	private TextView Vibrator_Value;
	float Vibrator_Reading = 0;
	float Vibrator_Pulse;
	private int Vibrator_FREQ=50;
	private int Vibrator_State;
	
	private final int Polling_Delay = 150;
	private long LastChange;

	
	/**
	 * Called when the activity is first created. Here we normally initialize
	 * our GUI.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		LED_Btn = (ToggleButton) findViewById(R.id.LED_Btn);
		LED_Btn.setOnClickListener(this);
		
		Lamp_Btn = (ToggleButton) findViewById(R.id.Lamp_Btn);
		Lamp_Btn.setOnClickListener(this);

		Smell_Btn = (ToggleButton) findViewById(R.id.Smell_Btn);
		Smell_Btn.setOnClickListener(this);
		
		Heat_Bar = (SeekBar) findViewById(R.id.Heat_Bar);
		Heat_Bar.setOnSeekBarChangeListener(this);
		Heat_Bar.setProgress(Heat_State);
		PWM = (TextView) findViewById(R.id.PWM_Txt);
		pulseHandler = new Handler();
		
		analog_Value = (TextView) findViewById(R.id.Analog_Value);
		Analog_Bar = (SeekBar) findViewById(R.id.Analog_Bar);
		
		Vibrator_Value = (TextView) findViewById(R.id.Vibrator_Value);
		Vibrator_Bar = (SeekBar) findViewById(R.id.Vibrator_Bar);
		Vibrator_Bar.setOnSeekBarChangeListener(this);
		Vibrator_Bar.setProgress(Vibrator_State);
		
		
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
		private DigitalOutput LED;
		private DigitalOutput Lamp;
		private DigitalOutput Smell;
		private DigitalOutput Analog_LED;
		private PwmOutput Motor;
		private PwmOutput Vibrator;
		private AnalogInput mAnalog;

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
				LED = ioio_.openDigitalOutput(LED_PIN, true);
				Lamp = ioio_.openDigitalOutput(Lamp_PIN, false);
				Smell = ioio_.openDigitalOutput(Smell_PIN, false);
				
				Motor = ioio_.openPwmOutput(new DigitalOutput.Spec(Heat_PIN, Mode.OPEN_DRAIN), Heat_FREQ);
				mAnalog = ioio_.openAnalogInput(Analog_PIN);
				Analog_LED = ioio_.openDigitalOutput(Analog_LED_PIN);

				Vibrator = ioio_.openPwmOutput(new DigitalOutput.Spec(Vibrator_PIN, Mode.OPEN_DRAIN), Vibrator_FREQ);
				
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
				LED.write(LED_State);
				Lamp.write(Lamp_State);
				Smell.write(Smell_State);
				
				pulse = 600 + Heat_Bar.getProgress() * 20;
				if(pulse <700) pulse=700;
				if(pulse > 2400) pulse = 2400;
				Motor.setPulseWidth(pulse);
				if(!LED_State)System.out.println ("Motor: "+pulse);
				
				Vibrator_Pulse =Vibrator_Bar.getProgress()*30;
				//Vibrator_FREQ =Vibrator_Bar.getProgress()*20;
				//if(pulse <700) pulse=700;
				//if(pulse > 2400) pulse = 2400;
				Vibrator.setPulseWidth(Vibrator_Pulse);
				if(!LED_State)System.out.println ("Vibrator= "+Vibrator_Pulse);
				
				Analog_Reading = mAnalog.read();
				Analog_Bar.setProgress((int)(Analog_Reading*1000));
				if(!LED_State)System.out.println("Heat Input "+ Analog_Reading *1000);
				if (Analog_Reading * 1000 > 10){
					Analog_LED.write(true);
				}else {
					Analog_LED.write(false);
				}
				//Analog_Txt.setText(Float.toString((reading * 100)));
				Thread.sleep(100);
			} catch (InterruptedException e) {
				enableUi(false);
				e.printStackTrace(); 
			}
			Runnable runnable = new Runnable(){
				public void run(){
					pulseHandler.post(new Runnable(){
						public void run(){
							PWM.setText("Pulse: " + pulse);
							analog_Value.setText("Value: "+Analog_Reading*1000);
							Vibrator_Value.setText("Value: "+Vibrator_Pulse);
						}
					});
				}
			};
			new Thread(runnable).start();
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
				Heat_Bar.setEnabled(enable);
			}
		});
	}
	
	@Override
	public void onClick(View v){
		switch (v.getId()){
		case R.id.LED_Btn:
			if (LED_State == true) {
				LED_State = false;
			} else {
				LED_State = true;
			}
			break;
			
		case R.id.Lamp_Btn:
			if (Lamp_State == true) {
				Lamp_State = false;
			} else {
				Lamp_State = true;
			}
			break;
			
		case R.id.Smell_Btn:
			Smell_State = true;
			
			//wait for time t and then run
			final Handler handler = new Handler();
			handler.postDelayed(new Runnable() { 
			   @Override
			   public void run() {
				   // on launch of activity we execute an async task 
				   Smell_State = false;
				   Smell_Btn.setChecked(false);
			   }
			}, 750);
			break;
		
		default:
			break;
		}
	}
	
	@Override
	public void onProgressChanged(SeekBar Seekbar, int progress, boolean fromUser){
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
		Heat_State = seekbar.getProgress();
		Vibrator_State = seekbar.getProgress();
		//Vibrator_FREQ = seekbar.getProgress();
	}
}