//
//  ZendriveCordovaPlugin.h
//

#import <Cordova/CDV.h>

@interface ZendriveCordovaPlugin : CDVPlugin

- (void)setup:(CDVInvokedUrlCommand*)command;
- (void)teardown:(CDVInvokedUrlCommand*)command;

- (void)startDrive:(CDVInvokedUrlCommand*)command;
- (void)stopDrive:(CDVInvokedUrlCommand*)command;

- (void)startSession:(CDVInvokedUrlCommand*)command;
- (void)stopSession:(CDVInvokedUrlCommand*)command;

- (void)setDriveDetectionMode:(CDVInvokedUrlCommand *)command;

- (void)pickupPassenger:(CDVInvokedUrlCommand*)command;
- (void)dropoffPassenger:(CDVInvokedUrlCommand*)command;
- (void)acceptPassengerRequest:(CDVInvokedUrlCommand*)command;
- (void)cancelPassengerRequest:(CDVInvokedUrlCommand*)command;
- (void)goOnDuty:(CDVInvokedUrlCommand*)command;
- (void)goOffDuty:(CDVInvokedUrlCommand*)command;

@end
