//
//  SLDirectionsViewController.h
//  Skylock
//
//  Created by Andre Green on 9/11/15.
//  Copyright (c) 2015 Andre Green. All rights reserved.
//

#import <UIKit/UIKit.h>
@class SLDirectionsViewController;
@protocol SLDirectionsViewControllerDelegate <NSObject>

- (void)directionsViewControllerWantsExit:(SLDirectionsViewController *)directionsController;

@end

@interface SLDirectionsViewController : UIViewController <UITableViewDataSource, UITableViewDelegate>

@property (nonatomic, strong) NSArray *directions;
@property (nonatomic, copy) NSString *endAddress;

@property (nonatomic, weak) id <SLDirectionsViewControllerDelegate> delegate;
@end
