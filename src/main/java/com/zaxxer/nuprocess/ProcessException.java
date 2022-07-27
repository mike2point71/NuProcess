package com.zaxxer.nuprocess;

import com.sun.jna.Native;

public class ProcessException extends RuntimeException{
  String failureMessage;
  int rc;
  int nativeRc;

  public ProcessException(String failureMessage, int rc, int nativeRc){
    super(failureMessage + ", return code: " + rc + ", last error: " + nativeRc);
    this.failureMessage = failureMessage;
    this.rc = rc;
    this.nativeRc = nativeRc;
  }
}
