//
//  SLShareingBaseTableViewCell.m
//  Skylock
//
//  Created by Andre Green on 6/29/15.
//  Copyright (c) 2015 Andre Green. All rights reserved.
//

#import "SLShareingBaseTableViewCell.h"
#import "SLContact.h"

@implementation SLShareingBaseTableViewCell

- (void)updateInfoWithContact:(SLContact *)contact
{
    self.textLabel.text = contact.fullName;
    self.imageView.image = [UIImage imageNamed:@"how do we get this image?"];
}

@end
