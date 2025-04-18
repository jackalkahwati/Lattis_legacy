//
//  UIColor+RGB.m
//  Skylock
//
//  Created by Andre Green on 7/12/15.
//  Copyright (c) 2015 Andre Green. All rights reserved.
//

#import "UIColor+RGB.h"

@implementation UIColor (RGB)

+ (id)colorWithRed:(CGFloat)red green:(CGFloat)green blue:(CGFloat)blue
{
    return [UIColor colorWithRed:red/255.0f green:green/255.0f blue:blue/255.0f alpha:1.0f];
}

@end
