//
//  SLCirclePicView.m
//  Skylock
//
//  Created by Andre Green on 7/15/15.
//  Copyright (c) 2015 Andre Green. All rights reserved.
//

#import "SLCirclePicView.h"
#import "NSString+Skylock.h"
#import "UIColor+RGB.h"

@interface SLCirclePicView()

@property (nonatomic, strong) UILabel *nameLabel;
@property (nonatomic, copy) NSString *name;
@property (nonatomic, assign) CGFloat picRadius;
@property (nonatomic, strong) UIImageView *picView;
@property (nonatomic, strong) UIColor *labelColor;
@property (nonatomic, assign) CGFloat verticalOffset;

@end


@implementation SLCirclePicView

- (id)initWithFrame:(CGRect)frame
               name:(NSString *)name
          picRadius:(CGFloat)picRadius
         labelColor:(UIColor *)labelColor
     verticalOffset:(CGFloat)verticalOffset
{
    self = [super initWithFrame:frame];
    if (self) {
        _name = name;
        _picRadius = picRadius;
        _labelColor = labelColor;
        _verticalOffset = verticalOffset;
        
        self.userInteractionEnabled = YES;
        UITapGestureRecognizer *tgr = [[UITapGestureRecognizer alloc] initWithTarget:self
                                                                              action:@selector(picViewTouched)];
        tgr.numberOfTapsRequired = 1;
        [self addGestureRecognizer:tgr];
    }
    
    return self;
}

- (UIImageView *)picView
{
    if (!_picView) {
        _picView = [[UIImageView alloc] initWithFrame:CGRectMake(0.0f,
                                                                 0.0f,
                                                                 2*self.picRadius,
                                                                 2*self.picRadius)];
        _picView.clipsToBounds = YES;
        _picView.layer.cornerRadius = self.picRadius;
        [self addSubview:_picView];
    }
    
    return _picView;
}

- (UILabel *)nameLabel
{
    if (!_nameLabel) {
        CGSize maxSize = CGSizeMake(self.bounds.size.width,
                                    self.bounds.size.height - self.picView.bounds.size.height);
        UIFont *font = [UIFont fontWithName:@"HelveticaNeue" size:8.0f];
        CGSize size = [self.name sizeWithFont:font maxSize:maxSize];
        _nameLabel = [[UILabel alloc] initWithFrame:CGRectMake(0.0f,
                                                               0.0,
                                                               size.width,
                                                               size.height)];
        _nameLabel.numberOfLines = 2;
        _nameLabel.text = self.name;
        _nameLabel.textAlignment = NSTextAlignmentCenter;
        _nameLabel.font = font;
        _nameLabel.textColor = self.labelColor ? self.labelColor : [UIColor colorWithRed:146
                                                                                   green:148
                                                                                    blue:151];
        
        [self addSubview:_nameLabel];
    }
    
    return _nameLabel;
}

- (void)setPicImage:(UIImage *)image
{
    self.picView.image = image;
    [self setNeedsDisplay];
}

- (void)layoutSubviews
{
    [super layoutSubviews];
    
    self.picView.frame = CGRectMake(.5*(self.bounds.size.width - self.picView.bounds.size.width),
                                    self.verticalOffset,
                                    self.picView.bounds.size.width,
                                    self.picView.bounds.size.height);
    
    self.nameLabel.frame = CGRectMake(.5*(self.bounds.size.width - self.nameLabel.bounds.size.width),
                                      CGRectGetMaxY(self.picView.frame) + 5.0f,
                                      self.nameLabel.bounds.size.width,
                                      self.nameLabel.bounds.size.height);
}

- (void)picViewTouched
{
    NSLog(@"%@ %@", NSStringFromClass([self class]), NSStringFromSelector(_cmd));
    if ([self.delegate respondsToSelector:@selector(circlePicViewPressed:)]) {
        [self.delegate circlePicViewPressed:self];
    }
}
@end
