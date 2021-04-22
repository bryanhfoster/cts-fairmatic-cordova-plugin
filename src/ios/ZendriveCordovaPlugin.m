//
//  ZendriveCordovaPlugin.m
//

#import "ZendriveCordovaPlugin.h"
#import <ZendriveSDK/Zendrive.h>
#import <ZendriveSDK/ZendriveActiveDriveInfo.h>
#import <ZendriveSDK/ZendriveInsurance.h>

static NSString * trackingId = @"0ICU812";

#pragma mark - Common dictionary keys
static NSString * const kTrackingIdKey = @"trackingId";
static NSString * const kSessionIdKey = @"sessionId";

#pragma mark - ZendriveDriveStartInfo dictionary keys
static NSString * const kStartTimestampKey = @"startTimestamp";
static NSString * const kStartLocationKey = @"startLocation";

#pragma mark - ZendriveDriveInfo dictionary keys
static NSString * const kEndTimestampKey = @"endTimestamp";
static NSString * const kAverageSpeedKey = @"averageSpeed";
static NSString * const kDistanceKey = @"distance";
static NSString * const kWaypointsKey = @"waypoints";

#pragma mark - DriverAttributes
static NSString * const kDriverStartDateKey = @"driverStartDate";
static NSString * const kDriverFirstNameKey = @"firstName";
static NSString * const kDriverLastNameKey = @"lastName";
static NSString * const kDriverEmailKey = @"email";
static NSString * const kDriverGroupKey = @"group";
static NSString * const kDriverPhoneNumberKey = @"phoneNumber";

static NSString * const kInsurancePeriodKey = @"insurancePeriod";



#pragma mark - ZendriveConfiguration
static NSString * const kConfigurationApplicationKey = @"applicationKey";
static NSString * const kConfigurationDriverIdKey = @"driverId";
static NSString * const kConfigurationDriveDetectionModeKey = @"driveDetectionMode";

#pragma mark - SetupKeys
static NSString * const kCustomAttributesKey = @"customAttributes";
static NSString * const kDriverAttributesKey = @"driverAttributes";

@interface ZendriveCordovaPlugin()<ZendriveDelegateProtocol>

// Delegate callback ids
@property (nonatomic, strong) NSString *processStartOfDriveCallbackId;
@property (nonatomic, strong) NSString *processEndOfDriveCallbackId;
@property (nonatomic, strong) NSString *processLocationDeniedCallbackId;
@property (nonatomic, strong) NSString *processLocationApprovedCallbackId;
@property (nonatomic, strong) NSString *processAnalysisOfDriveCallbackId;
@end

@implementation ZendriveCordovaPlugin

- (void)setup:(CDVInvokedUrlCommand*)command
{
    [self.commandDelegate runInBackground:^{
        @synchronized(self) {
            NSDictionary *configDictionary = [command argumentAtIndex:0];
            ZendriveConfiguration *configuration = [self configurationFromDictionary:configDictionary];

            [Zendrive
             setupWithConfiguration:configuration
             delegate:self
             completionHandler:^(BOOL success, NSError *error) {
                 CDVPluginResult* pluginResult = nil;
                 if(error == nil){
                     pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
                 }
                 else {
                     pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                      messageAsString:error.localizedFailureReason];
                 }
                 [self.commandDelegate sendPluginResult:pluginResult
                                             callbackId:command.callbackId];
             }];
        }
    }];
}

- (void)goOnDuty:(CDVInvokedUrlCommand*)command{
    [self.commandDelegate runInBackground:^{
        @synchronized(self) {
			trackingId = nil;
           [ZendriveInsurance startDriveWithPeriod1:^(BOOL success, NSError * _Nullable error) {
                 CDVPluginResult* pluginResult = nil;
                 if(error == nil){
					 NSLog(@"Insurance period 1 successfully called (goOnDuty).");
                     pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
                 }
                 else {
					 NSLog(@"Error encountered while starting period 1 (goOnDuty). Error is: %li", (long)error.code);
                     pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                      messageAsString:error.localizedFailureReason];
                 }
                 [self.commandDelegate sendPluginResult:pluginResult
                                             callbackId:command.callbackId];
            }];
        }
    }];
}


- (void)acceptPassengerRequest:(CDVInvokedUrlCommand*)command{
    [self.commandDelegate runInBackground:^{
        @synchronized(self) {
			int randomNumber = abs(rand() * 10000);
			trackingId = [NSString stringWithFormat:@"%i", randomNumber];
            [ZendriveInsurance startDriveWithPeriod2:trackingId 
				completionHandler:^(BOOL success, NSError * _Nullable error) {
                 CDVPluginResult* pluginResult = nil;
                 if(error == nil){
					 NSLog(@"Insurance period 2 successfully called (acceptPassengerRequest).");
                     pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
                 }
                 else {
					 NSLog(@"Error encountered while starting period 2 (acceptPassengerRequest). Error is: %li", (long)error.code);
                     pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                      messageAsString:error.localizedFailureReason];
                 }
                 [self.commandDelegate sendPluginResult:pluginResult
                                             callbackId:command.callbackId];
            }];
        }
    }];
}

- (void)pickupPassenger:(CDVInvokedUrlCommand*)command{
    [self.commandDelegate runInBackground:^{
        @synchronized(self) {
			if (trackingId == nil) {
				int randomNumber = abs(rand() * 10000);
				trackingId = [NSString stringWithFormat:@"%i", randomNumber];
			}		
            [ZendriveInsurance startDriveWithPeriod3:trackingId
				completionHandler:^(BOOL success, NSError * _Nullable error) {
                 CDVPluginResult* pluginResult = nil;
                 if(error == nil){
					 NSLog(@"Insurance period 3 successfully called (pickupPassenger).");
                     pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
                 }
                 else {
					 NSLog(@"Error encountered while starting period 3 (pickupPassenger). Error is: %li", (long)error.code);
                     pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                      messageAsString:error.localizedFailureReason];
                 }
                 [self.commandDelegate sendPluginResult:pluginResult
                                             callbackId:command.callbackId];
            }];
        }
    }];  
}


- (void)dropoffPassenger:(CDVInvokedUrlCommand*)command{
    [self.commandDelegate runInBackground:^{
        @synchronized(self) {
			trackingId = nil;
           [ZendriveInsurance startDriveWithPeriod1:^(BOOL success, NSError * _Nullable error) {
                 CDVPluginResult* pluginResult = nil;
                 if(error == nil){
					 NSLog(@"Insurance period 1 successfully called (dropoffPassenger).");
                     pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
                 }
                 else {
					 NSLog(@"Error encountered while starting period 1 (dropoffPassenger). Error is: %li", (long)error.code);
                     pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                      messageAsString:error.localizedFailureReason];
                 }
                 [self.commandDelegate sendPluginResult:pluginResult
                                             callbackId:command.callbackId];
            }];
        }
    }];
}

- (void)cancelPassengerRequest:(CDVInvokedUrlCommand*)command{
    [self.commandDelegate runInBackground:^{
        @synchronized(self) {
			trackingId = nil;
            [ZendriveInsurance startDriveWithPeriod1:^(BOOL success, NSError * _Nullable error) {
                 CDVPluginResult* pluginResult = nil;
                 if(error == nil){
					 NSLog(@"Insurance period 1 successfully called (cancelPassengerRequest).");
                     pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
                 }
                 else {
					 NSLog(@"Error encountered while starting period 1 (cancelPassengerRequest). Error is: %li", (long)error.code);
                     pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                      messageAsString:error.localizedFailureReason];
                 }
                 [self.commandDelegate sendPluginResult:pluginResult
                                             callbackId:command.callbackId];
            }];
        }
    }];
}

- (void)goOffDuty:(CDVInvokedUrlCommand*)command{
    [self.commandDelegate runInBackground:^{
        @synchronized(self) {
			trackingId = nil;
            [ZendriveInsurance stopPeriod:^(BOOL success, NSError * _Nullable error) {
                 CDVPluginResult* pluginResult = nil;
                 if(error == nil){
					 NSLog(@"Stop period successfully called (goOffDuty).");
                     pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
                 }
                 else {
					 NSLog(@"Error encountered while stopping period (goOffDuty). Error is: %li", (long)error.code);
                     pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                      messageAsString:error.localizedFailureReason];
                 }
                 [self.commandDelegate sendPluginResult:pluginResult
                                             callbackId:command.callbackId];
            }];
        }
    }];
}

- (void)getActiveDriveInfo:(CDVInvokedUrlCommand*)command{
    [self.commandDelegate runInBackground:^{
        @synchronized(self) {
            ZendriveActiveDriveInfo *activeDriveInfo = [Zendrive activeDriveInfo];
            NSMutableDictionary *activeDriveInfoDictionary;
            if (activeDriveInfo) {
                activeDriveInfoDictionary = [[NSMutableDictionary alloc] init];
                activeDriveInfoDictionary[kStartTimestampKey] = @(activeDriveInfo.startTimestamp);
                activeDriveInfoDictionary[kTrackingIdKey] = [NSNull null];
                if (activeDriveInfo.trackingId) {
                    activeDriveInfoDictionary[kTrackingIdKey] = activeDriveInfo.trackingId;
                }
                if (activeDriveInfo.insurancePeriod){
                    activeDriveInfoDictionary[kInsurancePeriodKey] = @(activeDriveInfo.insurancePeriod);
                }
                activeDriveInfoDictionary[kSessionIdKey] = [NSNull null];
                if (activeDriveInfo.sessionId) {
                    activeDriveInfoDictionary[kSessionIdKey] = activeDriveInfo.sessionId;
                }
            }
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                          messageAsDictionary:activeDriveInfoDictionary];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }
    }];
}

-(void)teardown:(CDVInvokedUrlCommand *)command{
    /*[self.commandDelegate runInBackground:^{
        @synchronized(self) {
            [Zendrive teardownWithCompletionHandler:^(BOOL success, NSError * _Nullable error) {
				if (success) {
					NSLog(@"Teardown successfully called.");
				}
				else {
					if (error) {
						NSLog(@"Error encountered while doing teardown", (long)error.code);
					}
				}
            }];
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }
    }];*/
}

- (void)startDrive:(CDVInvokedUrlCommand*)command{
	//TODO: this doesn't seem to be implemented anymore do nothing for now
    /*[self.commandDelegate runInBackground:^{
        @synchronized(self) {
            NSString *trackingId = [command argumentAtIndex:0];
            [Zendrive startDrive:trackingId];
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }
    }];*/
}

- (void)stopDrive:(CDVInvokedUrlCommand*)command{
    /*[self.commandDelegate runInBackground:^{
        @synchronized(self) {
            NSString *trackingId = [command argumentAtIndex:0];
            [Zendrive stopDrive:trackingId];
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }
    }];*/
}

- (void)startSession:(CDVInvokedUrlCommand*)command{
    [self.commandDelegate runInBackground:^{
        @synchronized(self) {
            [Zendrive startSession:[command argumentAtIndex:0]];
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }
    }];
}

- (void)stopSession:(CDVInvokedUrlCommand*)command{
    [self.commandDelegate runInBackground:^{
        @synchronized(self) {
            [Zendrive stopSession];
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }
    }];
}

- (void)setDriveDetectionMode:(CDVInvokedUrlCommand *)command{
    [self.commandDelegate runInBackground:^{
        @synchronized(self) {
            CDVPluginResult *pluginResult;
            NSNumber *modeNsNum = [command argumentAtIndex:0];
            ZendriveDriveDetectionMode driveDetectionMode = modeNsNum.intValue;
			[Zendrive setDriveDetectionMode:driveDetectionMode
                  completionHandler:^(BOOL success, NSError * _Nullable error) {
					    if (success) {
							NSLog(@"Drive detection mode successfully set to %d",driveDetectionMode);
						}
						else {
							if (error) {
								NSLog(@"Failed to set drive detection mode with error code:%ld, description:%@",(long)error.code,error.description);
							}
						}
                  }];

            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }
    }];
}

#pragma mark - Delegate callbacks
- (void)setProcessStartOfDriveDelegateCallback:(CDVInvokedUrlCommand*)command {
    if (self.processStartOfDriveCallbackId) {
        // Delete the old callback
        // Sending NO_RESULT doesn't call any js callback method
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];

        // Setting keepCallbackAsBool to NO would make sure that the callback is deleted from
        // memory after this call
        [pluginResult setKeepCallbackAsBool:NO];
        [self.commandDelegate sendPluginResult:pluginResult
                                    callbackId:self.processStartOfDriveCallbackId];
    }

    NSNumber *hasCallbackNsNum = [command argumentAtIndex:0];
    if (hasCallbackNsNum.boolValue) {
        self.processStartOfDriveCallbackId = command.callbackId;
    }
    else {
        self.processStartOfDriveCallbackId = nil;
    }
}

- (void)setProcessEndOfDriveDelegateCallback:(CDVInvokedUrlCommand*)command {
    if (self.processEndOfDriveCallbackId) {
        // Delete the old callback
        // Sending NO_RESULT doesn't call any js callback method
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];

        // Setting keepCallbackAsBool to NO would make sure that the callback is deleted from
        // memory after this call
        [pluginResult setKeepCallbackAsBool:NO];
        [self.commandDelegate sendPluginResult:pluginResult
                                    callbackId:self.processEndOfDriveCallbackId];
    }

    NSNumber *hasCallbackNsNum = [command argumentAtIndex:0];
    if (hasCallbackNsNum.boolValue) {
        self.processEndOfDriveCallbackId = command.callbackId;
    }
    else {
        self.processEndOfDriveCallbackId = nil;
    }
}

- (void)setProcessLocationDeniedDelegateCallback:(CDVInvokedUrlCommand*)command {
    if (self.processLocationDeniedCallbackId) {
        // Delete the old callback
        // Sending NO_RESULT doesn't call any js callback method
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];

        // Setting keepCallbackAsBool to NO would make sure that the callback is deleted from
        // memory after this call
        [pluginResult setKeepCallbackAsBool:NO];
        [self.commandDelegate sendPluginResult:pluginResult
                                    callbackId:self.processLocationDeniedCallbackId];
    }

    NSNumber *hasCallbackNsNum = [command argumentAtIndex:0];
    if (hasCallbackNsNum.boolValue) {
        self.processLocationDeniedCallbackId = command.callbackId;
    }
    else {
        self.processLocationDeniedCallbackId = nil;
    }
}

- (void)setProcessLocationApprovedDelegateCallback:(CDVInvokedUrlCommand*)command {
    if (self.processLocationApprovedCallbackId) {
        // Delete the old callback
        // Sending NO_RESULT doesn't call any js callback method
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];

        // Setting keepCallbackAsBool to NO would make sure that the callback is deleted from
        // memory after this call
        [pluginResult setKeepCallbackAsBool:NO];
        [self.commandDelegate sendPluginResult:pluginResult
                                    callbackId:self.processLocationApprovedCallbackId];
    }

    NSNumber *hasCallbackNsNum = [command argumentAtIndex:0];
    if (hasCallbackNsNum.boolValue) {
        self.processLocationApprovedCallbackId = command.callbackId;
    }
    else {
        self.processLocationApprovedCallbackId = nil;
    }
}


#pragma mark - ZendriveDelegateProtocol
- (void)processStartOfDrive:(ZendriveDriveStartInfo *)startInfo {
    if (!self.processStartOfDriveCallbackId) {
        return;
    }
    id startLocationDictionary = [NSNull null];
    if (startInfo.waypoints && startInfo.waypoints.count > 0) {
        startLocationDictionary = [startInfo.waypoints[0] toDictionary];
    }
    NSDictionary *startInfoDictionary = @{kStartTimestampKey:@(startInfo.startTimestamp),
                                          kStartLocationKey:startLocationDictionary};

    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                  messageAsDictionary:startInfoDictionary];
    [pluginResult setKeepCallbackAsBool:YES];
    [self.commandDelegate sendPluginResult:pluginResult
                                callbackId:self.processStartOfDriveCallbackId];
}

- (void)processEndOfDrive:(ZendriveDriveInfo *)driveInfo {
    if (!self.processEndOfDriveCallbackId) {
        return;
    }
    NSMutableArray *waypointsArray = [[NSMutableArray alloc] init];
    for (ZendriveLocationPoint *waypoint in driveInfo.waypoints) {
        NSDictionary *waypointDictionary = [waypoint toDictionary];
        [waypointsArray addObject:waypointDictionary];
    }
    NSDictionary *driveInfoDictionary = @{kStartTimestampKey:@(driveInfo.startTimestamp),
                                          kEndTimestampKey:@(driveInfo.endTimestamp),
                                          kAverageSpeedKey:@(driveInfo.averageSpeed),
                                          kDistanceKey:@(driveInfo.distance),
                                          kWaypointsKey:waypointsArray};

    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                  messageAsDictionary:driveInfoDictionary];
    [pluginResult setKeepCallbackAsBool:YES];
    [self.commandDelegate sendPluginResult:pluginResult
                                callbackId:self.processEndOfDriveCallbackId];
}

- (void)processAnalysisOfDrive:(ZendriveAnalyzedDriveInfo *)drive {
    if (!self.processAnalysisOfDriveCallbackId) {
        return;
    }
        NSLog(@"Analysis of Drive invoked");
    }


- (void)processLocationDenied {
    if (!self.processLocationDeniedCallbackId) {
        return;
    }
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [pluginResult setKeepCallbackAsBool:YES];
    [self.commandDelegate sendPluginResult:pluginResult
                                callbackId:self.processLocationDeniedCallbackId];
}

- (void)processLocationApproved {
    if (!self.processLocationApprovedCallbackId) {
        return;
    }
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [pluginResult setKeepCallbackAsBool:YES];
    [self.commandDelegate sendPluginResult:pluginResult
                                callbackId:self.processLocationApprovedCallbackId];
}

#pragma mark - Utility methods

- (ZendriveDriverAttributes *)driverAttrsFromDictionary:(NSDictionary *)driverAttrsDictionary {
    if ([self isNULL:driverAttrsDictionary]) {
        return nil;
    }

    ZendriveDriverAttributes *driverAttributes = [[ZendriveDriverAttributes alloc] init];
	NSString *firstName = [driverAttrsDictionary objectForKey:kDriverFirstNameKey];
    if (![self isNULL:firstName] && firstName.length > 0) {
        [driverAttributes setAlias:firstName];
    }
    //NSNumber *startDateTimestamp = [driverAttrsDictionary objectForKey:kDriverStartDateKey];
    //if (![self isNULL:startDateTimestamp]) {
    //    [driverAttributes setDriverStartDate:
    //     [NSDate dateWithTimeIntervalSince1970:startDateTimestamp.longValue]];
    //}
	//
    //NSString *firstName = [driverAttrsDictionary objectForKey:kDriverFirstNameKey];
    //if (![self isNULL:firstName]) {
    //    [driverAttributes setFirstName:firstName];
    //}
	//
    //NSString *lastName = [driverAttrsDictionary objectForKey:kDriverLastNameKey];
    //if (![self isNULL:lastName]) {
    //    [driverAttributes setLastName:lastName];
    //}
	//
    //NSString *email = [driverAttrsDictionary objectForKey:kDriverEmailKey];
    //if (![self isNULL:email]) {
    //    [driverAttributes setEmail:email];
    //}
	//
    //NSString *group = [driverAttrsDictionary objectForKey:kDriverGroupKey];
    //if (![self isNULL:group]) {
    //    [driverAttributes setGroup:group];
    //}
	//
    //NSString *phoneNumber = [driverAttrsDictionary objectForKey:kDriverPhoneNumberKey];
    //if (![self isNULL:phoneNumber]) {
    //    [driverAttributes setPhoneNumber:phoneNumber];
    //}
	//
    //NSDictionary *customAttributes = [driverAttrsDictionary objectForKey:kCustomAttributesKey];
    //if (![self isNULL:customAttributes]) {
    //    for (NSString *key in customAttributes.allKeys) {
    //        [driverAttributes setCustomAttribute:customAttributes[key] forKey:key];
    //    }
    //}

    return driverAttributes;
}

- (ZendriveConfiguration *)configurationFromDictionary:(NSDictionary *)configDictionary {
    if ([self isNULL:configDictionary]) {
        return nil;
    }

    ZendriveConfiguration *configuration = [[ZendriveConfiguration alloc] init];
  
    NSString *applicationKey = [configDictionary objectForKey:kConfigurationApplicationKey];
    if (![self isNULL:applicationKey]) {
        configuration.applicationKey = applicationKey;
    }

    NSString *driverId = [configDictionary objectForKey:kConfigurationDriverIdKey];
    if (![self isNULL:driverId]) {
        configuration.driverId = driverId;
    }

    NSNumber *driveDetectionMode = [configDictionary objectForKey:kConfigurationDriveDetectionModeKey];
    configuration.driveDetectionMode = ZendriveDriveDetectionModeInsurance;

    NSDictionary *driverAttrsDictionary = [configDictionary objectForKey:kDriverAttributesKey];
    ZendriveDriverAttributes *driverAttrs = [self driverAttrsFromDictionary:driverAttrsDictionary];
    if (![self isNULL:driverAttrs]) {
        [configuration setDriverAttributes:driverAttrs];
    }

    return configuration;
}

- (BOOL)isNULL:(NSObject *)object {
    if (!object || (object == [NSNull null])) {
        return YES;
    }
    return NO;
}
@end