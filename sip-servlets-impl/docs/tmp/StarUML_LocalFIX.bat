echo off

rem ////////////////////////////////////////////////////////////////////////////
rem //---------------------StarUML Decimal Separator Fix----------------------//
rem //                                                                        //
rem // Author: Kamil Œliwak (cameel2@gmail.com)                               //
rem //    Ver: 1.0.0.1                                                        //
rem //   Date: 21-04-2006 22:42:58                                            //
rem //   Desc: This script changes symbol used as decimal separator in        //
rem //         floating-point numbers in your system to period (.), runs      //
rem //         StarUML, and then restores the original separator. This fixes  //
rem //         the problem of a messagebox saying "'xx.xx' is not a valid     //
rem //         floating point value" appearing when trying to use             //
rem //         [Format]->[Layout Diagram] feature and having decimal          //
rem //         separator set to something else than a period.                 //
rem //                                                                        //
rem //-------------------------------Usage------------------------------------//
rem //                                                                        //
rem // Simply put this file in the same folder as the StarUML.exe file        //
rem // and use it to run the application.                                     //
rem //                                                                        //
rem //-------------------------------Notes------------------------------------//
rem //                                                                        //
rem // 1) This script works with any decimal separator that may be set in     //
rem //    your registry (it does not necessarily have to be a comma).         //
rem //                                                                        //
rem // 2) If this workaround doesn't work for you, try changing the for loop  //
rem //    count from 1000 to some higher number to cause more delay. 1000 is  //
rem //    enough on my PIII 850 Mhz, but if you have a fast computer, the     //
rem //    loop might end before StarUML starts and locale would be restored   //
rem //    prematurely. You can also remove /S flag in the line where regedit  //
rem //    is called for the second time - a message box will appear and       //
rem //    settings won't be restored until you agree to merge them into       //
rem //    registry.                                                           //
rem //                                                                        //
rem ////////////////////////////////////////////////////////////////////////////

rem Make period a new decimal separator
regedit /E _tmpfixo.reg "HKEY_CURRENT_USER\Control Panel\International"

echo REGEDIT4                                         > _tmpfixn.reg
echo [HKEY_CURRENT_USER\Control Panel\International] >> _tmpfixn.reg
echo "sDecimal"="."                                  >> _tmpfixn.reg

regedit /S _tmpfixn.reg

rem Launch StarUML
start StarUML

rem start command causes that this script doesn't wait for the application to
rem terminate but continues immediately. But we must ensure that StarUML
rem is started before we can restore original separator. Thus we have to
rem cause some delay by issuing some lenghty command
for /L %%i in (1000,-1,1) do echo restoring original decimal separator in %%i

rem Change decimal separator to period
rem echo REGEDIT4                                         > _tmpfix_.reg
rem echo [HKEY_CURRENT_USER\Control Panel\International] >> _tmpfix_.reg
rem echo "sDecimal"=","                                  >> _tmpfix_.reg

regedit /S _tmpfixo.reg

rem Remove temporary file
del _tmpfixo.reg
del _tmpfixn.reg

echo on
