//
//  SLNavigationViewController.m
//  Skylock
//
//  Created by Andre Green on 6/21/15.
//  Copyright (c) 2015 Andre Green. All rights reserved.
//

#import "SLNavigationViewController.h"
#import "SLConstants.h"

@interface SLNavigationViewController ()

@end

@implementation SLNavigationViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.navigationBar.translucent = NO;
    self.view.backgroundColor = [UIColor whiteColor];
    
    NSDictionary *barOptions = @{NSForegroundColorAttributeName:SLConstantsNavControllerTitleColor,
                                 NSFontAttributeName:SLConstantsNavControllerFont
                                 };
    
    self.navigationBar.titleTextAttributes = barOptions;
    self.navigationBar.tintColor = [UIColor blackColor];
}



@end
