package com.example.brittanyhsu.bhspotify;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.gracenote.gnsdk.GnAlbum;
import com.gracenote.gnsdk.GnAlbumIterator;
import com.gracenote.gnsdk.GnAssetFetch;
import com.gracenote.gnsdk.GnDescriptor;
import com.gracenote.gnsdk.GnError;
import com.gracenote.gnsdk.GnException;
import com.gracenote.gnsdk.GnLanguage;
import com.gracenote.gnsdk.GnLicenseInputMode;
import com.gracenote.gnsdk.GnList;
import com.gracenote.gnsdk.GnLocale;
import com.gracenote.gnsdk.GnLocaleGroup;
import com.gracenote.gnsdk.GnLog;
import com.gracenote.gnsdk.GnLookupData;
import com.gracenote.gnsdk.GnLookupLocalStream;
import com.gracenote.gnsdk.GnLookupLocalStreamIngest;
import com.gracenote.gnsdk.GnLookupLocalStreamIngestStatus;
import com.gracenote.gnsdk.GnManager;
import com.gracenote.gnsdk.GnMic;
import com.gracenote.gnsdk.GnMusicId;
import com.gracenote.gnsdk.GnMusicIdFile;
import com.gracenote.gnsdk.GnMusicIdStream;
import com.gracenote.gnsdk.GnMusicIdStreamIdentifyingStatus;
import com.gracenote.gnsdk.GnMusicIdStreamPreset;
import com.gracenote.gnsdk.GnMusicIdStreamProcessingStatus;
import com.gracenote.gnsdk.GnRegion;
import com.gracenote.gnsdk.GnResponseAlbums;
import com.gracenote.gnsdk.GnStatus;
import com.gracenote.gnsdk.GnStorageSqlite;
import com.gracenote.gnsdk.GnUser;
import com.gracenote.gnsdk.GnUserStore;
import com.gracenote.gnsdk.IGnAudioSource;
import com.gracenote.gnsdk.IGnCancellable;
import com.gracenote.gnsdk.IGnLookupLocalStreamIngestEvents;
import com.gracenote.gnsdk.IGnMusicIdStreamEvents;
import com.gracenote.gnsdk.IGnSystemEvents;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class FingerprintActivity extends AppCompatActivity {

    // set these values before running the sample
    static final String 				gnsdkClientId 			=  Constants.GNSDK_CLIENT_ID;
    static final String 				gnsdkClientTag 			= Constants.GNSDK_CLIENT_TAG;
    static final String 				gnsdkLicenseFilename 	= "mylicensefile.txt";	// app expects this file as an "asset"
    private static final String    		gnsdkLogFilename 		= "sample.log";
    private static final String 		appString				= "BHSpotifyxShazam";

    private Activity activity;
    private Context context;

    // ui objects
    private TextView statusText;

//    private SettingsMenu				settingsMenu;
    private Button 						buttonIDNow;
    private ImageView                   buttonFingerprint;

    // Gracenote objects
    private GnManager gnManager;
    protected static GnUser gnUser;
    private GnMusicIdStream gnMusicIdStream;
    private IGnAudioSource gnMicrophone;
    private List<GnMusicIdStream>		streamIdObjects			= new ArrayList<GnMusicIdStream>();

    // store some tracking info about the most recent MusicID-Stream lookup
    protected volatile boolean 			lastLookup_local		 = false;	// indicates whether the match came from local storage
    protected volatile long				lastLookup_matchTime 	 = 0;  		// total lookup time for query
    protected volatile long				lastLookup_startTime;  				// start time of query
    private volatile boolean			audioProcessingStarted   = false;

    public String accessToken = "";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        if(intent.getStringExtra("access token") != null)
            accessToken = intent.getStringExtra("access token");

        createUI();

        activity = this;
        context  = this.getApplicationContext();

        // check the client id and tag have been set
        if ( (gnsdkClientId == null) || (gnsdkClientTag == null) ){
            showError( "Please set Client ID and Client Tag" );
            return;
        }

        // get the gnsdk license from the application assets
        String gnsdkLicense = null;
        if ( (gnsdkLicenseFilename == null) || (gnsdkLicenseFilename.length() == 0) ){
            showError( "License filename not set" );
        } else {
            gnsdkLicense = getAssetAsString( gnsdkLicenseFilename );
            if ( gnsdkLicense == null ){
                showError( "License file not found: " + gnsdkLicenseFilename );
                return;
            }
        }

        try {

            // GnManager must be created first, it initializes GNSDK
            gnManager = new GnManager( context, gnsdkLicense, GnLicenseInputMode.kLicenseInputModeString );

            // provide handler to receive system events, such as locale update needed
            gnManager.systemEventHandler( new SystemEvents() );

            // get a user, if no user stored persistently a new user is registered and stored
            // Note: Android persistent storage used, so no GNSDK storage provider needed to store a user
            gnUser = new GnUser( new GnUserStore(context), gnsdkClientId, gnsdkClientTag, appString );

            // enable storage provider allowing GNSDK to use its persistent stores
            GnStorageSqlite.enable();

            // enable local MusicID-Stream recognition (GNSDK storage provider must be enabled as pre-requisite)
            GnLookupLocalStream.enable();

            // Loads data to support the requested locale, data is downloaded from Gracenote Service if not
            // found in persistent storage. Once downloaded it is stored in persistent storage (if storage
            // provider is enabled). Download and write to persistent storage can be lengthy so perform in
            // another thread
            Thread localeThread = new Thread(
                    new LocaleLoadRunnable(GnLocaleGroup.kLocaleGroupMusic,
                            GnLanguage.kLanguageEnglish,
                            GnRegion.kRegionGlobal,
                            GnDescriptor.kDescriptorDefault,
                            gnUser)
            );
            localeThread.start();

            // Ingest MusicID-Stream local bundle, perform in another thread as it can be lengthy
            Thread ingestThread = new Thread( new LocalBundleIngestRunnable(context) );
            ingestThread.start();

            // Set up for continuous listening from the microphone
            // - create microphone, this can live for lifetime of app
            // - create GnMusicIdStream instance, this can live for lifetime of app
            // - configure
            // Starting and stopping continuous listening should be started and stopped
            // based on Activity life-cycle, see onPause and onResume for details
            // To show audio visualization we wrap GnMic in a visualization adapter
            gnMicrophone = new AudioVisualizeAdapter( new GnMic() );
            gnMusicIdStream = new GnMusicIdStream( gnUser, GnMusicIdStreamPreset.kPresetMicrophone, new MusicIDStreamEvents() );
            gnMusicIdStream.options().lookupData(GnLookupData.kLookupDataContent, true);
            gnMusicIdStream.options().lookupData(GnLookupData.kLookupDataSonicData, true);
            gnMusicIdStream.options().resultSingle( true );

            // Retain GnMusicIdStream object so we can cancel an active identification if requested
            streamIdObjects.add( gnMusicIdStream );

        } catch ( GnException e ) {

            Log.e(appString, e.errorCode() + ", " + e.errorDescription() + ", " + e.errorModule() );
            showError( e.errorAPI() + ": " + e.errorDescription() );
            return;

        } catch ( Exception e ) {
            if(e.getMessage() != null){
                Log.e(appString, e.getMessage() );
                showError( e.getMessage() );
            }
            else{
                e.printStackTrace();
            }
            return;

        }

        setStatus( "" , true );
        setUIState( UIState.READY );
    }

    protected static GnUser getGnUser() {
        return gnUser;
    }

    protected static void setGnUser(GnUser gnUser) {
        FingerprintActivity.gnUser = gnUser;
    }

    @Override
    protected void onResume() {
        super.onResume();
        statusText.setVisibility(View.INVISIBLE);

        if ( gnMusicIdStream != null ) {

            // Create a thread to process the data pulled from GnMic
            // Internally pulling data is a blocking call, repeatedly called until
            // audio processing is stopped. This cannot be called on the main thread.
            Thread audioProcessThread = new Thread(new AudioProcessRunnable());
            audioProcessThread.start();

        }

        // tmp - work around temporary behavior where
        // calling audioProcessStop stops all events, including
        // cancelled notification, from a pending identification
        if ( gnManager != null ) {
            setStatus( "", true );
            setUIState( UIState.READY );
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        if ( gnMusicIdStream != null ) {

            try {

                // to ensure no pending identifications deliver results while your app is
                // paused it is good practice to call cancel
                // it is safe to call identifyCancel if no identify is pending
                gnMusicIdStream.identifyCancel();

                // stopping audio processing stops the audio processing thread started
                // in onResume
                gnMusicIdStream.audioProcessStop();

            } catch (GnException e) {

                Log.e( appString, e.errorCode() + ", " + e.errorDescription() + ", " + e.errorModule() );
                showError( e.errorAPI() + ": " +  e.errorDescription() );

            }

        }
    }

//    public void onBackPressed(){
//        final ProgressBar pg = (ProgressBar) findViewById(R.id.progressBar);
//        if(pg.isShown())
//            pg.setVisibility(View.INVISIBLE);
//    }


    private void createUI() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_fingerprint);

//        final ProgressBar pg = (ProgressBar) findViewById(R.id.progressBar);
//        pg.setVisibility(View.INVISIBLE);

        final LottieAnimationView lottieAnimationView = (LottieAnimationView) findViewById(R.id.loadingAnimation);
        lottieAnimationView.setAnimation("search.json");
        lottieAnimationView.setScale(10);

//        if(lottieAnimationView.isAnimating())
//            lottieAnimationView.cancelAnimation();

        lottieAnimationView.loop(true);
        lottieAnimationView.playAnimation();

//        buttonIDNow = (Button) findViewById(R.id.buttonIDNow);
        buttonFingerprint = (ImageView) findViewById(R.id.loadingAnimation);
        buttonFingerprint.setEnabled( false );
        buttonFingerprint.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    setUIState( UIState.INPROGRESS );
                    clearResults();

                    try {

                        gnMusicIdStream.identifyAlbumAsync();
                        lastLookup_startTime = SystemClock.elapsedRealtime();

                    } catch (GnException e) {

                        Log.e( appString, e.errorCode() + ", " + e.errorDescription() + ", " + e.errorModule() );
                        showError( e.errorAPI() + ": " +  e.errorDescription() );

                    }
                    return true;
                }
                return false;
            }
        });
//        buttonFingerprint.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                lottieAnimationView.loop(true);
//                lottieAnimationView.playAnimation();
//                setUIState( UIState.INPROGRESS );
//                clearResults();
//
//                try {
//
//                    gnMusicIdStream.identifyAlbumAsync();
//                    lastLookup_startTime = SystemClock.elapsedRealtime();
//
//                } catch (GnException e) {
//
//                    Log.e( appString, e.errorCode() + ", " + e.errorDescription() + ", " + e.errorModule() );
//                    showError( e.errorAPI() + ": " +  e.errorDescription() );
//
//                }
//            }
//        });

        statusText = (TextView) findViewById(R.id.statusText);

    }

    /**
     * Audio visualization adapter.
     * Sits between GnMic and GnMusicIdStream to receive audio data as it
     * is pulled from the microphone allowing an audio visualization to be
     * implemented.
     */
    class AudioVisualizeAdapter implements IGnAudioSource {

        private IGnAudioSource 	audioSource;
        private int				numBitsPerSample;
        private int				numChannels;

        public AudioVisualizeAdapter( IGnAudioSource audioSource ){
            this.audioSource = audioSource;
        }

        @Override
        public long sourceInit() {
            if ( audioSource == null ){
                return 1;
            }
            long retVal = audioSource.sourceInit();

            // get format information for use later
            if ( retVal == 0 ) {
                numBitsPerSample = (int)audioSource.sampleSizeInBits();
                numChannels = (int)audioSource.numberOfChannels();
            }

            return retVal;
        }

        @Override
        public long numberOfChannels() {
            return numChannels;
        }

        @Override
        public long sampleSizeInBits() {
            return numBitsPerSample;
        }

        @Override
        public long samplesPerSecond() {
            if ( audioSource == null ){
                return 0;
            }
            return audioSource.samplesPerSecond();
        }

        @Override
        public long getData(ByteBuffer buffer, long bufferSize) {
            if ( audioSource == null ){
                return 0;
            }

            long numBytes = audioSource.getData(buffer, bufferSize);

            if ( numBytes != 0 ) {
                // perform visualization effect here
                // Note: Since API level 9 Android provides android.media.audiofx.Visualizer which can be used to obtain the
                // raw waveform or FFT, and perform measurements such as peak RMS. You may wish to consider Visualizer class
                // instead of manually extracting the audio as shown here.
                // This sample does not use Visualizer so it can demonstrate how you can access the raw audio for purposes
                // not limited to visualization.
//                audioVisualizeDisplay.setAmplitudePercent(rmsPercentOfMax(buffer,bufferSize,numBitsPerSample,numChannels), true);
            }

            return numBytes;
        }

        @Override
        public void sourceClose() {
            if ( audioSource != null ){
                audioSource.sourceClose();
            }
        }

        // calculate the rms as a percent of maximum
        private int rmsPercentOfMax( ByteBuffer buffer, long bufferSize, int numBitsPerSample, int numChannels) {
            double rms = 0.0;
            if ( numBitsPerSample == 8 ) {
                rms = rms8( buffer, bufferSize, numChannels );
                return (int)((rms*100)/(double)((double)(Byte.MAX_VALUE/2)));
            } else {
                rms = rms16( buffer, bufferSize, numChannels );
                return (int)((rms*100)/(double)((double)(Short.MAX_VALUE/2)));
            }
        }

        // calculate the rms of a buffer containing 8 bit audio samples
        private double rms8 ( ByteBuffer buffer, long bufferSize, int numChannels ) {

            long sum = 0;
            long numSamplesPerChannel = bufferSize/numChannels;

            for(int i = 0; i < numSamplesPerChannel; i+=numChannels)
            {
                byte sample = buffer.get();
                sum += (sample * sample);
            }

            return Math.sqrt( (double)(sum / numSamplesPerChannel) );
        }

        // calculate the rms of a buffer containing 16 bit audio samples
        private double rms16 ( ByteBuffer buffer, long bufferSize, int numChannels ) {

            long sum = 0;
            long numSamplesPerChannel = (bufferSize/2)/numChannels;	// 2 bytes per sample

            buffer.rewind();
            for(int i = 0; i < numSamplesPerChannel; i++)
            {
                short sample = Short.reverseBytes(buffer.getShort()); // reverse because raw data is little endian but Java short is big endian

                sum += (sample * sample);
                if ( numChannels == 2 ){
                    buffer.getShort();
                }
            }

            return Math.sqrt( (double)(sum / numSamplesPerChannel) );
        }
    }

    /**
     * GnMusicIdStream object processes audio read directly from GnMic object
     */
    class AudioProcessRunnable implements Runnable {

        @Override
        public void run() {
            try {

                // start audio processing with GnMic, GnMusicIdStream pulls data from GnMic internally
                gnMusicIdStream.audioProcessStart( gnMicrophone );

            } catch (GnException e) {

                Log.e( appString, e.errorCode() + ", " + e.errorDescription() + ", " + e.errorModule() );
                showError( e.errorAPI() + ": " +  e.errorDescription() );

            }
        }
    }

    /**
     * Loads a locale
     */
    class LocaleLoadRunnable implements Runnable {
        GnLocaleGroup	group;
        GnLanguage		language;
        GnRegion		region;
        GnDescriptor	descriptor;
        GnUser			user;


        LocaleLoadRunnable(
                GnLocaleGroup group,
                GnLanguage		language,
                GnRegion		region,
                GnDescriptor	descriptor,
                GnUser			user) {
            this.group 		= group;
            this.language 	= language;
            this.region 	= region;
            this.descriptor = descriptor;
            this.user 		= user;
        }

        @Override
        public void run() {
            try {

                GnLocale locale = new GnLocale(group,language,region,descriptor,gnUser);
                locale.setGroupDefault();

            } catch (GnException e) {
                Log.e(appString, e.errorCode() + ", " + e.errorDescription() + ", " + e.errorModule());
            }
        }
    }

    /**
     * Updates a locale
     */
    class LocaleUpdateRunnable implements Runnable {
        GnLocale		locale;
        GnUser			user;


        LocaleUpdateRunnable(
                GnLocale		locale,
                GnUser			user) {
            this.locale 	= locale;
            this.user 		= user;
        }

        @Override
        public void run() {
            try {
                locale.update(user);
            } catch (GnException e) {
                Log.e( appString, e.errorCode() + ", " + e.errorDescription() + ", " + e.errorModule() );
            }
        }
    }

    /**
     * Updates a list
     */
    class ListUpdateRunnable implements Runnable {
        GnList list;
        GnUser			user;


        ListUpdateRunnable(
                GnList			list,
                GnUser			user) {
            this.list 		= list;
            this.user 		= user;
        }

        @Override
        public void run() {
            try {
                list.update(user);
            } catch (GnException e) {
                Log.e( appString, e.errorCode() + ", " + e.errorDescription() + ", " + e.errorModule() );
            }
        }
    }


    /**
     * Loads a local bundle for MusicID-Stream lookups
     */
    class LocalBundleIngestRunnable implements Runnable {
        Context context;

        LocalBundleIngestRunnable(Context context) {
            this.context = context;
        }

        public void run() {
            try {

                // our bundle is delivered as a package asset
                // to ingest the bundle access it as a stream and write the bytes to
                // the bundle ingester
                // bundles should not be delivered with the package as this, rather they
                // should be downloaded from your own online service

                InputStream bundleInputStream 	= null;
                int				ingestBufferSize	= 1024;
                byte[] 			ingestBuffer 		= new byte[ingestBufferSize];
                int				bytesRead			= 0;

                GnLookupLocalStreamIngest ingester = new GnLookupLocalStreamIngest(new BundleIngestEvents());

                try {

                    bundleInputStream = context.getAssets().open("1557.b");

                    do {

                        bytesRead = bundleInputStream.read(ingestBuffer, 0, ingestBufferSize);
                        if ( bytesRead == -1 )
                            bytesRead = 0;

                        ingester.write( ingestBuffer, bytesRead );

                    } while( bytesRead != 0 );

                } catch (IOException e) {
                    e.printStackTrace();
                }

                ingester.flush();

            } catch (GnException e) {
                Log.e( appString, e.errorCode() + ", " + e.errorDescription() + ", " + e.errorModule() );
            }

        }
    }


    /**
     * Receives system events from GNSDK
     */
    class SystemEvents implements IGnSystemEvents {
        @Override
        public void localeUpdateNeeded( GnLocale locale ){

            // Locale update is detected
            Thread localeUpdateThread = new Thread(new LocaleUpdateRunnable(locale,gnUser));
            localeUpdateThread.start();
        }

        @Override
        public void listUpdateNeeded( GnList list ) {
            // List update is detected
            Thread listUpdateThread = new Thread(new ListUpdateRunnable(list,gnUser));
            listUpdateThread.start();
        }

        @Override
        public void systemMemoryWarning(long currentMemorySize, long warningMemorySize) {
            // only invoked if a memory warning limit is configured
        }
    }

    /**
     * GNSDK MusicID-Stream event delegate
     */
    private class MusicIDStreamEvents implements IGnMusicIdStreamEvents {

        HashMap<String, String> gnStatus_to_displayStatus;

        public MusicIDStreamEvents(){
            gnStatus_to_displayStatus = new HashMap<String,String>();
            gnStatus_to_displayStatus.put(GnMusicIdStreamIdentifyingStatus.kStatusIdentifyingStarted.toString(), "Identification started");
            gnStatus_to_displayStatus.put(GnMusicIdStreamIdentifyingStatus.kStatusIdentifyingFpGenerated.toString(), "Fingerprinting complete");
            gnStatus_to_displayStatus.put(GnMusicIdStreamIdentifyingStatus.kStatusIdentifyingLocalQueryStarted.toString(), "Lookup started");
            gnStatus_to_displayStatus.put(GnMusicIdStreamIdentifyingStatus.kStatusIdentifyingOnlineQueryStarted.toString(), "Lookup started");
//			gnStatus_to_displayStatus.put(GnMusicIdStreamIdentifyingStatus.kStatusIdentifyingEnded.toString(), "Identification complete");
        }

        @Override
        public void statusEvent( GnStatus status, long percentComplete, long bytesTotalSent, long bytesTotalReceived, IGnCancellable cancellable ) {

        }

        @Override
        public void musicIdStreamProcessingStatusEvent(GnMusicIdStreamProcessingStatus status, IGnCancellable canceller ) {

            if(GnMusicIdStreamProcessingStatus.kStatusProcessingAudioStarted.compareTo(status) == 0)
            {
                audioProcessingStarted = true;
                activity.runOnUiThread(new Runnable (){
                    public void run(){
                        buttonFingerprint.setEnabled(true);
                    }
                });

            }

        }

        @Override
        public void musicIdStreamIdentifyingStatusEvent( GnMusicIdStreamIdentifyingStatus status, IGnCancellable canceller ) {
            if(gnStatus_to_displayStatus.containsKey(status.toString())){
                setStatus( String.format("%s", gnStatus_to_displayStatus.get(status.toString())), true );
            }

            if(status.compareTo( GnMusicIdStreamIdentifyingStatus.kStatusIdentifyingLocalQueryStarted ) == 0 ){
                lastLookup_local = true;
            }
            else if(status.compareTo( GnMusicIdStreamIdentifyingStatus.kStatusIdentifyingOnlineQueryStarted ) == 0){
                lastLookup_local = false;
            }

            if ( status == GnMusicIdStreamIdentifyingStatus.kStatusIdentifyingEnded )
            {
                setUIState( UIState.READY );
            }
        }


        @Override
        public void musicIdStreamAlbumResult( GnResponseAlbums result, IGnCancellable canceller ) {
            lastLookup_matchTime = SystemClock.elapsedRealtime() - lastLookup_startTime;
            activity.runOnUiThread(new UpdateResultsRunnable( result ));
        }

        @Override
        public void musicIdStreamIdentifyCompletedWithError(GnError error) {
            if ( error.isCancelled() )
                setStatus( "Cancelled", true );
            else
                setStatus( error.errorDescription(), true );
            setUIState( UIState.READY );
        }
    }


    /**
     * GNSDK bundle ingest status event delegate
     */
    private class BundleIngestEvents implements IGnLookupLocalStreamIngestEvents {

        @Override
        public void statusEvent(GnLookupLocalStreamIngestStatus status, String bundleId, IGnCancellable canceller) {
            setStatus("Bundle ingest progress: " + status.toString() , true);
        }
    }


    /**
     * Helpers to read license file from assets as string
     */
    private String getAssetAsString( String assetName ){

        String 		assetString = null;
        InputStream assetStream;

        try {

            assetStream = this.getApplicationContext().getAssets().open(assetName);
            if(assetStream != null){

                java.util.Scanner s = new java.util.Scanner(assetStream).useDelimiter("\\A");

                assetString = s.hasNext() ? s.next() : "";
                assetStream.close();

            }else{
                Log.e(appString, "Asset not found:" + assetName);
            }

        } catch (IOException e) {

            Log.e( appString, "Error getting asset as string: " + e.getMessage() );

        }

        return assetString;
    }


    /**
     * Helpers to enable/disable the application widgets
     */
    enum UIState{
        DISABLED,
        READY,
        INPROGRESS
    }

    private void setUIState( UIState uiState ) {
        activity.runOnUiThread(new SetUIState(uiState));
    }

    class SetUIState implements Runnable {

        UIState uiState;
        SetUIState( UIState uiState ){
            this.uiState = uiState;
        }

        @Override
        public void run() {

            boolean enabled = (uiState == UIState.READY);

            buttonFingerprint.setEnabled( enabled && audioProcessingStarted);
        }

    }

    /**
     * Helper to set the application status message
     */
    private void setStatus( String statusMessage, boolean clearStatus ){
        activity.runOnUiThread(new UpdateStatusRunnable( statusMessage, clearStatus ));
    }

    class UpdateStatusRunnable implements Runnable {

        boolean clearStatus;
        String status;

        UpdateStatusRunnable( String status, boolean clearStatus ){
            this.status = status;
            this.clearStatus = clearStatus;
        }

        @Override
        public void run() {
            statusText.setVisibility(View.VISIBLE);
            if (clearStatus) {
                statusText.setText(status);
            } else {
                statusText.setText(statusText.getText() + "\n" + status);
            }
        }

    }

    /**
     * Helpers to load and set cover art image in the application display
     */
    void loadAndDisplayCoverArt(String coverArtUrl, ImageView imageView ){
        Thread runThread = new Thread( new CoverArtLoaderRunnable( coverArtUrl, imageView ) );
        runThread.start();
    }

    class CoverArtLoaderRunnable implements Runnable {

        String 	coverArtUrl;
        ImageView 	imageView;

        CoverArtLoaderRunnable( String coverArtUrl, ImageView imageView){
            this.coverArtUrl = coverArtUrl;
            this.imageView = imageView;
        }

        @Override
        public void run() {

            Drawable coverArt = null;

            if (coverArtUrl != null && !coverArtUrl.isEmpty()) {
                try {
                    GnAssetFetch assetData = new GnAssetFetch(gnUser,coverArtUrl);
                    byte[] data = assetData.data();
                    coverArt =  new BitmapDrawable(BitmapFactory.decodeByteArray(data, 0, data.length));
                } catch (GnException e) {
                    e.printStackTrace();
                }

            }

            if (coverArt != null) {
                setCoverArt(coverArt, imageView);
            } else {
                setCoverArt(getResources().getDrawable(R.drawable.no_image),imageView);
            }

        }

    }

    private void setCoverArt( Drawable coverArt, ImageView coverArtImage ){
        activity.runOnUiThread(new SetCoverArtRunnable(coverArt, coverArtImage));
    }

    class SetCoverArtRunnable implements Runnable {

        Drawable coverArt;
        ImageView coverArtImage;

        SetCoverArtRunnable( Drawable locCoverArt, ImageView locCoverArtImage) {
            coverArt = locCoverArt;
            coverArtImage = locCoverArtImage;
        }

        @Override
        public void run() {
            coverArtImage.setImageDrawable(coverArt);
        }
    }

    /**
     * Adds album results to UI via Runnable interface
     */
    class UpdateResultsRunnable implements Runnable {

        GnResponseAlbums albumsResult;

        UpdateResultsRunnable(GnResponseAlbums albumsResult) {
            this.albumsResult = albumsResult;
        }

        @Override
        public void run() {
            try {
                if (albumsResult.resultCount() == 0) {

                    setStatus("No match", true);

                } else {

                    setStatus("Match found", true);
                    GnAlbumIterator iter = albumsResult.albums().getIterator();

                    while (iter.hasNext()) {
//                        updateMetaDataFields(iter.next(), true, false);
                        getTrackInfo(iter.next());
                    }
                    trackChanges(albumsResult);

                }
            } catch (GnException e) {
                setStatus(e.errorDescription(), true);
            }

        }
    }

    private void getTrackInfo(final GnAlbum album) {
        Log.d(appString, "getTrackInfo called");

        String artist = album.trackMatched().artist().name().display();
        String track =  album.trackMatched().title().display();
        if(!artist.isEmpty())
            Log.d(appString, "artist: " + artist);

            //use album artist if track artist not available
        else {
            artist = album.artist().name().display();
            Log.d(appString, "artist: " + artist);
        }

        if ( album.trackMatched() != null ) {
            Log.d(appString, "track: " + track);
            Intent searchIntent = new Intent(FingerprintActivity.this, ViewPagerActivity.class);
            searchIntent.putExtra("track", track)
                    .putExtra("artist", artist)
                    .putExtra("access token", accessToken);

            startActivity(searchIntent);
        }
    }



    /**
     * Helper to clear the results from the application display
     */
    private void clearResults() {
        statusText.setText("No recording");
//        metadataListing.removeAllViews();
    }

    /**
     * Helper to show and error
     */
    private void showError( String errorMessage ) {
        setStatus( errorMessage, true );
        setUIState( UIState.DISABLED );
    }


    /**
     * History Tracking:
     * initiate the process to insert values into database.
     *
     * @param albums
     *            - contains all the information to be inserted into DB,
     *            except location.
     */
    private synchronized void trackChanges(GnResponseAlbums albums) {
        Thread thread = new Thread (new InsertChangesRunnable(albums));
        thread.start();

    }

    class InsertChangesRunnable implements Runnable {
        GnResponseAlbums row;

        InsertChangesRunnable(GnResponseAlbums row) {
            this.row = row;
        }

        @Override
        public void run() {
            try {
                DatabaseAdapter db = new DatabaseAdapter(FingerprintActivity.this);
                db.open();
                db.insertChanges(row);
                db.close();
            } catch (GnException e) {
                // ignore
            }
        }
    }
}
