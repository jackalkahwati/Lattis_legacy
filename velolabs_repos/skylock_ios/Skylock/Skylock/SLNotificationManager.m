//
//  SLNotificationManager.m
//  Skylock
//
//  Created by Andre Green on 8/24/15.
//  Copyright (c) 2015 Andre Green. All rights reserved.
//

#import "SLNotificationManager.h"
#import "NSString+Skylock.h"
#import "SLNotification.h"
#import "SLNotifications.h"
#import "SLDatabaseManager.h"
#import "Ellipse-Swift.h"
#import <AudioToolbox/AudioServices.h>

@interface SLNotificationManager()

@property (nonatomic, strong) NSMutableArray *notifications;
@property (nonatomic, strong) NSDateFormatter *displayFormatter;
@property (nonatomic, strong) NSDateFormatter *fullFormatter;

@end

@implementation SLNotificationManager

- (id)init
{
    self = [super init];
    if (self) {
        _notifications = [NSMutableArray new];
    }
    
    return self;
}

+ (id)sharedManager
{
    NSLog(@"%@ %@", NSStringFromClass([self class]), NSStringFromSelector(_cmd));
    static SLNotificationManager *notificationManger = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        notificationManger = [[self alloc] init];
    });
    
    return notificationManger;
}

- (NSDateFormatter *)displayFormatter
{
    if (!_displayFormatter) {
        _displayFormatter = [NSDateFormatter new];
        [_displayFormatter setDateFormat:@"hh:mm a"];
    }
    
    return _displayFormatter;
}

- (NSDateFormatter *)fullFormatter
{
    if (!_fullFormatter) {
        _fullFormatter = [NSDateFormatter new];
        [_fullFormatter setDateStyle:NSDateFormatterFullStyle];
        [_fullFormatter setTimeStyle:NSDateFormatterFullStyle];
    }
    
    return _fullFormatter;
}

- (NSString *)formattedDisplayTimeForNotificiaton:(SLNotification *)notification
{
    return [self.displayFormatter stringFromDate:notification.date];
}

- (NSString *)formattedFullTimeForNotfication:(SLNotification *)notification
{
    return [self.fullFormatter stringFromDate:notification.date];
}

- (void)setNotificationDateString:(SLNotification *)notification
{
}

- (void)createNotificationOfType:(SLNotificationType)notficationType
                forLockWithMacId:(NSString *)macId
                     withAccInfo:(NSDictionary *)accInfo
{
    // do this since only one notification can be displayed at a time
    SLLock *lock = [[SLDatabaseManager shared] getLockWithMacId:macId];
    if (self.notifications.count == 0 && lock != nil) {
        SLNotification *notification = [[SLNotification alloc] initWithType:notficationType];
        notification.displayDateString = [self formattedDisplayTimeForNotificiaton:notification];
        notification.fullDateString = [self formattedFullTimeForNotfication:notification];
        notification.macId = macId;
        [self.notifications addObject:notification];
        AudioServicesPlayAlertSound(kSystemSoundID_Vibrate);
        [[NSNotificationCenter defaultCenter] postNotificationName:kSLNotificationAlertOccured
                                                            object:notification
                                                          userInfo:accInfo];
    }
}

- (NSArray *)getNotifications
{
    return self.notifications;
}

- (SLNotification *)lastNotification
{
    if (self.notifications.count == 0) {
        return nil;
    }
    
    return [self.notifications lastObject];
}

- (void)dismissNotificationWithId:(NSString *)notificationId
{
    NSUInteger index = 0;
    for (SLNotification *notification in self.notifications) {
        if ([notificationId isEqualToString:notification.identifier]) {
            break;
        }
        
        index++;
    }
    
    if (index < self.notifications.count) {
        SLNotification *notification = self.notifications[index];
        [self.notifications removeObjectAtIndex:index];
        [[NSNotificationCenter defaultCenter] postNotificationName:kSLNotificationAlertDismissed
                                                            object:nil
                                                          userInfo:@{@"notification":notification}];
    }
}

- (void)removeLastNotification
{
    if (self.notifications.count == 0) {
        return;
    }
    
    [self.notifications removeLastObject];
}

- (void)sendTheftAlertForLockWithMacId:(NSString *)macId withAccInfo:(NSDictionary * _Nonnull)accInfo
{
    [self createNotificationOfType:SLNotificationTypeTheft forLockWithMacId:macId withAccInfo:accInfo];
}

- (void)sendCrashAlertForLockWithMacId:(NSString *)macId withAccInfo:(NSDictionary * _Nonnull)accInfo
{
    [self createNotificationOfType:SLNotificationTypeCrashPre forLockWithMacId:macId withAccInfo:accInfo];
}

- (void)sendEmergencyText
{
    NSLog(@"Will send emergency text...soon");
}

@end
