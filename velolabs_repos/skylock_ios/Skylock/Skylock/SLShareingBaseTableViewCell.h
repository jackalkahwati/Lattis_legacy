//
//  SLShareingBaseTableViewCell.h
//  Skylock
//
//  Created by Andre Green on 6/29/15.
//  Copyright (c) 2015 Andre Green. All rights reserved.
//

#import <UIKit/UIKit.h>
@class SLContact;

@interface SLShareingBaseTableViewCell : UITableViewCell

- (void)updateInfoWithContact:(SLContact *)contact;

@end
