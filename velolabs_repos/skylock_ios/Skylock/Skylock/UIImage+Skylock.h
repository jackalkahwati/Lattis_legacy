//
//  UIImage+Skylock.h
//  Skylock
//
//  Created by Andre Green on 7/19/15.
//  Copyright (c) 2015 Andre Green. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface UIImage (Skylock)

- (UIImage *)resizedImageWithSize:(CGSize)newSize;
+ (UIImage *)profilePicFromImage:(UIImage *)image;

@end
