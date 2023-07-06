package com.plugin.SerialPortPlugin;


import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.locks.*;


class ReadDataThread implements Runnable {
   private Thread t;
   private String threadName;
   private InputStream input;
   private int readLen = 0;
   private String readData = "";
   private  Lock lock=new ReentrantLock();
   private boolean dataModel;
   private boolean running = true;

   ReadDataThread( String name, InputStream inputStream, boolean model) {
      threadName = name;
      input = inputStream;
      dataModel = model;
      System.out.println("Creating " +  threadName );
   }

   public void run() {
	 int readSize = -1;
     while(running) {
         try {
			 if((readSize = input.available()) <= 0) {  //get the buffer length before read. if you do not, the read will block
				try {
					Thread.sleep(1);
				} catch(Exception e) {
				}
				continue;
			 }
          } catch (IOException e) {
                e.printStackTrace();
      			System.out.println("Thread" +  threadName + " break exiting..");
				break;
          }
          System.out.println("readSize:" + readSize);
          byte[] byteArray = new byte[readSize];
          try {
                readLen = input.read(byteArray);
          } catch (IOException e) {
                e.printStackTrace();
      			System.out.println("Thread" +  threadName + " break exiting..");
				break;
          }
          lock.lock(); // must lock to copy readData
          if(this.dataModel == true) {
			 readData += FormatUtil.bytes2HexString(byteArray, readLen);
          } else {
			readData += new String(byteArray);
          }
          lock.unlock();
          System.out.println("readstr:" + readData);
      }

	  if(running) {
		 try {
			input.close();
		 } catch (IOException e) {
			e.printStackTrace();
		 }
	  }
      System.out.println("Thread" +  threadName + " success exiting..");
   }

   public void setHex(boolean model) {
      this.dataModel = model;
      System.out.println("dataModel:" + this.dataModel);
   }

   public String getData() {
        String data = null;
        lock.lock();
        if(readData != "") {
            data = new String(readData);
            readData = "";
        }
        lock.unlock();
        return data;
   }

   public void start() {
      System.out.println("Starting " +  threadName );
      if (t == null) {
         t = new Thread (this, threadName);
         t.start ();
      }
   }

   public void stop() {
	 this.running = false;
     try {
		input.close();
     } catch (IOException e) {
        e.printStackTrace();
     }
  }
}