//
//  SLBaseTableViewCell.h
//  Skylock
//
//  Created by Andre Green on 6/28/15.
//  Copyright (c) 2015 Andre Green. All rights reserved.
//

#import <UIKit/UIKit.h>

typedef NS_ENUM(NSUInteger, SLBaseTableViewCellProperty) {
    SLBaseTableViewCellPropertyTextLabel,
    SLBaseTableViewCellPropertyDetailTextLabel,
    SLBaseTableViewCellPropertyImageViewImage,
    SLBaseTableViewCellPropertyAccesoryView,
};


@interface SLBaseTableViewCell : UITableViewCell

- (NSDictionary *)updateCellWithInfo:(NSDictionary *)info;
- (BOOL)setPropertyWithKey:(NSNumber *)key andInfo:(NSDictionary *)info;

@end
