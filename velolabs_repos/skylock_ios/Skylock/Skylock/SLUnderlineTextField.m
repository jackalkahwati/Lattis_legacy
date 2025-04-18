//
//  SLUnderlineTextField.m
//  Skylock
//
//  Created by Andre Green on 7/12/15.
//  Copyright (c) 2015 Andre Green. All rights reserved.
//

#import "SLUnderlineTextField.h"

@interface SLUnderlineTextField()

@property (nonatomic, strong) UIView *underlineView;
@property (nonatomic, strong) UIColor *lineColor;

@end


@implementation SLUnderlineTextField

- (id)initWithFrame:(CGRect)frame lineColor:(UIColor *)lineColor
{
    self = [super initWithFrame:frame];
    if (self) {
        _lineColor = lineColor;
    }
    
    return self;
}

- (UIView *)underlineView
{
    if (!_underlineView) {
        _underlineView = [[UIView alloc] initWithFrame:CGRectMake(0.0f,
                                                                  0.0f,
                                                                  self.frame.size.width,
                                                                  1.0f)];
        _underlineView.backgroundColor = self.lineColor;
        [self addSubview:_underlineView];
    }
    
    return _underlineView;
}

- (void)layoutSubviews
{
    [super layoutSubviews];
    
    self.underlineView.frame = CGRectMake(0.0f,
                                          self.bounds.size.height - self.underlineView.bounds.size.height,
                                          self.underlineView.bounds.size.width,
                                          self.underlineView.bounds.size.height);
}
@end
