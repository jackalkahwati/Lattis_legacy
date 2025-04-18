//
//  UIImage+Skylock.m
//  Skylock
//
//  Created by Andre Green on 7/19/15.
//  Copyright (c) 2015 Andre Green. All rights reserved.
//

#import "UIImage+Skylock.h"

@implementation UIImage (Skylock)

- (UIImage *)resizedImageWithSize:(CGSize)newSize
{
    UIGraphicsBeginImageContext(newSize);
    [self drawInRect:CGRectMake(0,
                                0,
                                newSize.width,
                                newSize.height)];
    UIImage *resizedImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return resizedImage;
}

+ (UIImage *)profilePicFromImage:(UIImage *)image
{
    UIBezierPath *path = [UIBezierPath bezierPath];
    
    CGFloat carrotWidth = .15*image.size.width;
    CGFloat carrotHeight = .2*image.size.height;
    CGFloat radius = .5*(image.size.height - carrotHeight);
    CGFloat theta = acos(carrotWidth/radius);
    CGFloat startAngle = theta;
    CGFloat endAngle = M_PI - theta;
    
    CGPoint startPoint = CGPointMake(.5*image.size.width, image.size.height);
    CGPoint rightBottom = CGPointMake(image.size.width, image.size.height);
    CGPoint rightTop = CGPointMake(image.size.width, 0.0f);
    CGPoint leftTop = CGPointMake(0.0f, 0.0f);
    CGPoint leftBottom = CGPointMake(0.0f, image.size.height);

    CGPoint rightCarrot = CGPointMake(startPoint.x + carrotWidth, startPoint.y - carrotHeight);
    CGPoint center = CGPointMake(.5*image.size.width, radius);
    
    [path moveToPoint:startPoint];
    [path moveToPoint:rightBottom];
    [path moveToPoint:rightTop];
    [path moveToPoint:leftTop];
    [path moveToPoint:leftBottom];
    [path moveToPoint:startPoint];
    [path moveToPoint:rightCarrot];
    [path addArcWithCenter:center
                    radius:radius
                startAngle:startAngle
                  endAngle:endAngle
                 clockwise:NO];
    [path addLineToPoint:startPoint];
    [path closePath];
    
    UIImageView *maskedView = [[UIImageView alloc] initWithImage:image];
    CAShapeLayer *maskLayer = [CAShapeLayer layer];
    maskLayer.path = path.CGPath;
    maskedView.layer.mask = maskLayer;
    
    UIGraphicsBeginImageContext(maskedView.bounds.size);
    [maskedView.layer renderInContext:UIGraphicsGetCurrentContext()];
    UIImage *newImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    return newImage;
}

@end
