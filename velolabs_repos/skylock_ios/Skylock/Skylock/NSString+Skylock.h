//
//  NSString+Skylock.h
//  Skylock
//
//  Created by Andre Green on 6/19/15.
//  Copyright (c) 2015 Andre Green. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@interface NSString (Skylock)

- (instancetype)stringWithDistance:(NSNumber *)distance;

- (CGSize)sizeWithFont:(UIFont *)font maxSize:(CGSize)maxSize;

- (NSString *)MD5String;

- (NSData *)bytesString;

- (NSString *)macAddress;
@end
