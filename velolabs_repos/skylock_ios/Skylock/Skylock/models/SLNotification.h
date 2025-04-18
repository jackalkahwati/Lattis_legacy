//
//  SLNotification.h
//  Skylock
//
//  Created by Andre Green on 8/24/15.
//  Copyright (c) 2015 Andre Green. All rights reserved.
//

#import <Foundation/Foundation.h>
@class SLNotification;

typedef NS_ENUM(NSUInteger, SLNotificationType) {
    SLNotificationTypeNone,
    SLNotificationTypeTheft,
    SLNotificationTypeCrashPre,
    SLNotificationTypeCrashPost
};


@interface SLNotification : NSObject

@property (nonatomic, assign) SLNotificationType type;
@property (nonatomic, copy) NSString *detailText;
@property (nonatomic, copy) NSString *mainText;
@property (nonatomic, strong) NSDate *date;
@property (nonatomic, copy) NSString *displayDateString;
@property (nonatomic, copy) NSString *fullDateString;
@property (nonatomic, copy) NSString *identifier;
@property (nonatomic, copy) NSString *macId;

- (id)initWithType:(SLNotificationType)notificaitonType;

@end
