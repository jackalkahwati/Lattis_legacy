//
//  SLNotification.m
//  Skylock
//
//  Created by Andre Green on 8/24/15.
//  Copyright (c) 2015 Andre Green. All rights reserved.
//

#import "SLNotification.h"
#import "NSString+Skylock.h"

@implementation SLNotification

- (id)initWithType:(SLNotificationType)notificaitonType
{
    self = [super init];
    if (self) {
        _type = notificaitonType;
        _date = [NSDate date];
        [self setText];
    }
    
    return self;
}

- (NSString *)identifier
{
    if (!_identifier) {
        _identifier = [NSString stringWithFormat:@"%@_%@_%@",
                       self.mainText,
                       self.detailText,
                       self.fullDateString];
    }
    
    return _identifier;
}

- (void)setText
{
    switch (self.type) {
        case SLNotificationTypeCrashPre:
            self.mainText = NSLocalizedString(@"Ellipse Crash Alert", nil);
            self.detailText = NSLocalizedString(@"Ignore this or get some help", nil);
            break;
        case SLNotificationTypeCrashPost:
            self.mainText = NSLocalizedString(@"Ellipse Crash Alert", nil);
            self.detailText = NSLocalizedString(@"Emergency contacts notified", nil);
            break;
        case SLNotificationTypeTheft:
            self.mainText = NSLocalizedString(@"Ellipse Theft Alert", nil);
            self.detailText = NSLocalizedString(@"Theft Threat", nil);
            break;
        default:
            break;
    }
}

@end
