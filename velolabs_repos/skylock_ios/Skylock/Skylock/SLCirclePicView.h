//
//  SLCirclePicView.h
//  Skylock
//
//  Created by Andre Green on 7/15/15.
//  Copyright (c) 2015 Andre Green. All rights reserved.
//

#import <UIKit/UIKit.h>
@class SLCirclePicView;

@protocol SLCirclePicViewDelegate <NSObject>

- (void)circlePicViewPressed:(SLCirclePicView *)picView;

@end


@interface SLCirclePicView : UIView

@property (nonatomic, weak) id <SLCirclePicViewDelegate> delegate;

- (id)initWithFrame:(CGRect)frame name:(NSString *)name picRadius:(CGFloat)picRadius labelColor:(UIColor *)labelColor verticalOffset:(CGFloat)verticalOffset;

- (void)setPicImage:(UIImage *)image;

@end
