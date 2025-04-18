//
//  SLBaseTableViewCell.m
//  Skylock
//
//  Created by Andre Green on 6/28/15.
//  Copyright (c) 2015 Andre Green. All rights reserved.
//

#import "SLBaseTableViewCell.h"

@implementation SLBaseTableViewCell

- (NSDictionary *)updateCellWithInfo:(NSDictionary *)info
{
    NSMutableDictionary *baseCantHandle = [NSMutableDictionary new];
    for (NSNumber *key in info) {
        if ([self setPropertyWithKey:key andInfo:info]) {
            baseCantHandle[key] = info[key];
        }
    }
    
    return baseCantHandle;
}

- (BOOL)setPropertyWithKey:(NSNumber *)key andInfo:(NSDictionary *)info
{
    SLBaseTableViewCellProperty property = (SLBaseTableViewCellProperty)key.unsignedIntegerValue;
    BOOL canHandleKey = YES;
    switch (property) {
        case SLBaseTableViewCellPropertyTextLabel:
            self.textLabel.text = info[key];
            break;
        case SLBaseTableViewCellPropertyDetailTextLabel:
            self.detailTextLabel.text = info[key];
            break;
        case SLBaseTableViewCellPropertyImageViewImage:
            self.imageView.image = info[key];
            break;
        case SLBaseTableViewCellPropertyAccesoryView:
            self.accessoryView = info[key];
            break;
        default:
            canHandleKey = NO;
            break;
    }
    
    return canHandleKey;
}

@end
