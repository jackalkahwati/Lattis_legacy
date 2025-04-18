//
//  SLDirectionsViewController.m
//  Skylock
//
//  Created by Andre Green on 9/11/15.
//  Copyright (c) 2015 Andre Green. All rights reserved.
//

#import "SLDirectionsViewController.h"
#import "SLDirectionsTableViewCell.h"
#import "NSString+Skylock.h"
#import "Ellipse-Swift.h"


#define kSLDirectionViewControllerDirectionCellId @"kSLDirectionViewControllerDirectionCellId"

@interface SLDirectionsViewController()

@property (nonatomic, strong) UIView *headerView;
@property (nonatomic, strong) UITableView *tableView;
@property (nonatomic, assign) CGFloat xPadding;

@end

#define kSLDirectionsVCHeaderHeight 142.0f

@implementation SLDirectionsViewController

- (UIView *)headerView
{
    if (!_headerView) {
        _headerView = [[UIView alloc] initWithFrame:CGRectMake(0,
                                                               0,
                                                               self.view.bounds.size.width,
                                                               kSLDirectionsVCHeaderHeight)];
        
        UIImage *collapseDirectionsImage = [UIImage imageNamed:@"map_lock_info_x_button"];
        CGRect removeDirectionsButtonFrame = CGRectMake(self.view.bounds.size.width - self.xPadding - collapseDirectionsImage.size.width,
                                                        25.0f,
                                                        2.0f*collapseDirectionsImage.size.width,
                                                        2.0f*collapseDirectionsImage.size.height);
        UIButton *removeDirectionsButton = [[UIButton alloc] initWithFrame:removeDirectionsButtonFrame];
        [removeDirectionsButton addTarget:self action:@selector(collapseButtonPressed) forControlEvents:UIControlEventTouchDown];
        [removeDirectionsButton setImage:collapseDirectionsImage forState:UIControlStateNormal];
        [_headerView addSubview:removeDirectionsButton];
        
        NSString *headerText = NSLocalizedString(@"Walking directions", nil);
        CGSize maxSize = CGSizeMake(_headerView.bounds.size.width - 2*CGRectGetMaxX(removeDirectionsButton.frame) + 10.0f,
                                    CGFLOAT_MAX);
        
        UIFont *headerLabelFont = [UIFont fontWithName:@"Montserrat-Regular" size:18.0f];
        CGSize headerLabelSize = [headerText sizeWithFont:headerLabelFont maxSize:maxSize];
        UILabel *headerLabel = [[UILabel alloc] initWithFrame:CGRectMake(self.xPadding,
                                                                              CGRectGetMaxY(removeDirectionsButton.frame) + 15.0,
                                                                              headerLabelSize.width,
                                                                              headerLabelSize.height)];
        headerLabel.text = headerText;
        headerLabel.textAlignment = NSTextAlignmentLeft;
        headerLabel.font = headerLabelFont;
        headerLabel.textColor = [UIColor color:88 green:109 blue:144];
        [_headerView addSubview:headerLabel];
        
        UIFont *durationLabelFont = [UIFont fontWithName:@"OpenSans" size:11.0f];
        float totalDistance = [self totalDistance];
        NSString *durationFrontText = [NSString stringWithFormat:@"%.1f %@",
                                       totalDistance,
                                       NSLocalizedString(@"miles", "")
                                       ];
        maxSize = CGSizeMake(_headerView.bounds.size.width - 2*self.xPadding, CGFLOAT_MAX);
        
        
        CGSize durationFrontLabelSize = [durationFrontText sizeWithFont:durationLabelFont maxSize:maxSize];
        UILabel *durationFrontLabel = [[UILabel alloc] initWithFrame:CGRectMake(self.xPadding,
                                                                                CGRectGetMaxY(headerLabel.frame) + 25.0f,
                                                                                durationFrontLabelSize.width,
                                                                                durationFrontLabelSize.height)];
        durationFrontLabel.text = durationFrontText;
        durationFrontLabel.textAlignment = NSTextAlignmentCenter;
        durationFrontLabel.font = durationLabelFont;
        durationFrontLabel.textColor = [UIColor color:88 green:164 blue:212];
        durationFrontLabel.numberOfLines = 1;
        [_headerView addSubview:durationFrontLabel];
        float totalTime = [self totalTime];
        NSString *durationRearText = [NSString stringWithFormat:@" / %.1f %@",
                                       totalTime/60.0,
                                       NSLocalizedString(@"minute walk", nil)
                                       ];
        
        CGSize durationRearLabelSize = [durationRearText sizeWithFont:durationLabelFont maxSize:maxSize];
        UILabel *durationRearLabel = [[UILabel alloc] initWithFrame:CGRectMake(CGRectGetMaxX(durationFrontLabel.frame),
                                                                                CGRectGetMinY(durationFrontLabel.frame),
                                                                                durationRearLabelSize.width,
                                                                                durationRearLabelSize.height)];
        durationRearLabel.text = durationRearText;
        durationRearLabel.textAlignment = NSTextAlignmentCenter;
        durationRearLabel.font = durationLabelFont;
        durationRearLabel.textColor = [UIColor color:140 green:140 blue:140];
        durationRearLabel.numberOfLines = 1;
        [_headerView addSubview:durationRearLabel];
        
        [self.view addSubview:_headerView];
    }
    
    return _headerView;
}

- (UITableView *)tableView
{
    if (!_tableView) {
        CGFloat headerHeight = CGRectGetMaxY(self.headerView.frame);
        CGRect frame = CGRectMake(0.0f,
                                  headerHeight,
                                  self.view.bounds.size.width,
                                  self.view.bounds.size.height - headerHeight);
        
        _tableView = [[UITableView alloc] initWithFrame:frame style:UITableViewStylePlain];
        [_tableView registerClass:[SLDirectionsTableViewCell class]
           forCellReuseIdentifier:kSLDirectionViewControllerDirectionCellId];
        _tableView.delegate = self;
        _tableView.dataSource = self;
        _tableView.rowHeight = 90.0f;
        _tableView.layoutMargins = UIEdgeInsetsZero;
        _tableView.backgroundColor = [UIColor whiteColor];
        _tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
        
        [self.view addSubview:_tableView];
    }
    
    return _tableView;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    self.xPadding = 30.0f;
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    self.headerView.backgroundColor = [UIColor clearColor];
    
    [self.tableView reloadData];
}

- (void)collapseButtonPressed
{
    if ([self.delegate respondsToSelector:@selector(directionsViewControllerWantsExit:)]) {
        [self.delegate directionsViewControllerWantsExit:self];
    }
}

- (CGFloat)totalDistance
{
    CGFloat distance = 0.0;
    for (SLDirection *direction in self.directions) {
        distance += [direction distanceInMiles];
    }
    
    return distance;
}

- (float)totalTime
{
    float time = 0.0;
    for (SLDirection *direction in self.directions) {
        time += [direction getDuration];
    }
    
    return time;
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return self.directions.count + 1;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    SLDirectionsTableViewCell *cell = (SLDirectionsTableViewCell *)[tableView dequeueReusableCellWithIdentifier:kSLDirectionViewControllerDirectionCellId];
    SLDirection *direction;
    if (indexPath.row < self.directions.count) {
        direction = self.directions[indexPath.row];
    }
    
    NSString *topText;
    NSString *bottomText;
    if (indexPath.row == 0) {
        topText = NSLocalizedString(@"START", nil);
        bottomText = direction.directions;
    } else if (indexPath.row == self.directions.count) {
        topText = NSLocalizedString(@"END", nil);
        bottomText = self.endAddress;
    } else {
        topText = [NSString stringWithFormat:@"%@ %.1f%@",
                   NSLocalizedString(@"Walk", nil),
                   [direction distanceInMiles],
                   NSLocalizedString(@"mi then", nil)];
        bottomText = direction.directions;
    }
    NSDictionary *properties = @{@"top": topText,
                                 @"bottom": bottomText
                                 };
    
    [cell setPropertiesWithDirection:properties
                    isFirstDirection:indexPath.row == 0
                     isLastDirection:indexPath.row == self.directions.count];
    cell.selectionStyle = UITableViewCellSelectionStyleNone;
    
    return cell;
}

@end
