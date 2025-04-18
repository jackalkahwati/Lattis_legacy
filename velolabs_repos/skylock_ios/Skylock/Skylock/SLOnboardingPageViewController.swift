//
//  SLOnboardingController.swift
//  Skylock
//
//  Created by Andre Green on 5/26/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit

private enum SLOnboardingViewControllerInfo {
    case Pic
    case Title
    case Text
}

@objc public class SLOnboardingPageViewController:
UIViewController,
UIPageViewControllerDelegate,
UIPageViewControllerDataSource
{
    var pageIndex:Int = 0
    
    var onboardingControllers = [SLOnboardingViewController]()
    
    var toViewControllerIndex:Int = 0
    
    private let controllerData:[[SLOnboardingViewControllerInfo:String]] = [
        [
            .Pic: "onboarding_main_image_1",
            .Title: NSLocalizedString("Theft alerts", comment: ""),
            .Text: NSLocalizedString(
                "Ellipse can detect if someone’s tampering with your bike, " +
                "and send you an alert so you can take action.",
                comment: ""
            )
        ],
        [
            .Pic: "onboarding_main_image_2",
            .Title: NSLocalizedString(
                "Make the most of your bike by sharing with your friends.",
                comment: ""
            ),
            .Text: NSLocalizedString(
                "Ellipse's built in accelerometer can detect if you've been in a crash, and alert your loved ones.",
                comment: ""
            )
        ],
        [
            .Pic: "onboarding_main_image_3",
            .Title: NSLocalizedString(
                "Tap to unlock",
                comment: ""
            ),
            .Text: NSLocalizedString(
                "Using your smartphone, just one click safely locks or unlocks your Ellipse. " +
                "No more fumbling with bulky chains.",
                comment: ""
            )
        ],
        [
            .Pic: "onboarding_main_image_4",
            .Title: NSLocalizedString(
                "Easily locate your bike",
                comment: ""
            ),
            .Text: NSLocalizedString(
                "Can't remember where you parked? Use the map tool and get walking directions.",
                comment: ""
            )
        ]
    ]
        
    lazy var pageControl:UIPageControl = {
        let width:CGFloat = 100.0
        let frame = CGRect(
            x: 0.5*(self.view.bounds.size.width - width),
            y: 0.5*self.view.bounds.size.height - 90.0,
            width: width,
            height: 50
        )
        
        let control:UIPageControl = UIPageControl(frame: frame)
        control.numberOfPages = self.controllerData.count
        control.currentPage = 0
        
        return control
    }()
    
    lazy var pageViewController:UIPageViewController = {
        let pageController:UIPageViewController = UIPageViewController(
            transitionStyle: UIPageViewControllerTransitionStyle.scroll,
            navigationOrientation: UIPageViewControllerNavigationOrientation.horizontal,
            options: nil
        )
        pageController.setViewControllers(
            [self.onboardingControllers[0]],
            direction: UIPageViewControllerNavigationDirection.forward,
            animated: false,
            completion: nil
        )
        pageController.delegate = self
        pageController.dataSource = self
        
        return pageController
    }()
    
    lazy var getStartedButton:UIButton = {
        let height:CGFloat = 55.0
        let frame = CGRect(
            x: 0.0,
            y: self.view.bounds.size.height - height,
            width: self.view.bounds.size.width,
            height: height
        )
        
        let button:UIButton = UIButton(type: UIButtonType.system)
        button.frame = frame
        button.addTarget(
            self,
            action: #selector(getStartedButtonPressed),
            for: UIControlEvents.touchDown
        )
        button.setTitle(NSLocalizedString("GET STARTED", comment: ""), for: .normal)
        button.setTitleColor(UIColor.white, for: .normal)
        button.backgroundColor = UIColor.color(86, green: 216, blue: 255)
        button.titleLabel?.font = UIFont(name: SLFont.MontserratRegular.rawValue, size: 18.0)
        return button
    }()
    
    override public func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .white
        automaticallyAdjustsScrollViewInsets = false
        
        let obvc0 = SLOnboardingViewController(
            picName: self.controllerData[0][.Pic]!,
            titleText: self.controllerData[0][.Title]!,
            text: self.controllerData[0][.Text]!,
            yBottomBound: self.getStartedButton.frame.minY
        )
        
        let obvc1 = SLOnboardingViewController(
            picName: self.controllerData[1][.Pic]!,
            titleText: self.controllerData[1][.Title]!,
            text: self.controllerData[1][.Text]!,
            yBottomBound: self.getStartedButton.frame.minY
        )
        
        let obvc2 = SLOnboardingViewController(
            picName: self.controllerData[2][.Pic]!,
            titleText: self.controllerData[2][.Title]!,
            text: self.controllerData[2][.Text]!,
            yBottomBound: self.getStartedButton.frame.minY
        )
        
        let obvc3 = SLOnboardingMapBackgroundViewController(
            picName: self.controllerData[3][.Pic]!,
            titleText: self.controllerData[3][.Title]!,
            text: self.controllerData[3][.Text]!,
            yBottomBound: self.getStartedButton.frame.minY
        )
        
        self.onboardingControllers = [
            obvc0,
            obvc1,
            obvc2,
            obvc3
        ]
        
        pageViewController.willMove(toParentViewController: self)
        self.addChildViewController(self.pageViewController)
        self.view.addSubview(self.pageViewController.view)
        pageViewController.view.translatesAutoresizingMaskIntoConstraints = false
        pageViewController.view.constrainEdges(to: view)
        self.pageViewController.didMove(toParentViewController: self)
        
        self.view.addSubview(self.getStartedButton)
        self.view.addSubview(self.pageControl)
    }
    
    func getStartedButtonPressed() {
        let signInViewController = SLSignInViewController()
        self.navigationController?.setViewControllers([signInViewController], animated: true)
    }
    
    func indexForOnboardingViewController(onboardingViewController: SLOnboardingViewController) -> Int? {
        return self.onboardingControllers.index(of: onboardingViewController)
    }
    
    public func pageViewController(
        _ pageViewController: UIPageViewController,
        viewControllerAfter viewController: UIViewController
        ) -> UIViewController?
    {
        if let index = self.onboardingControllers.index(of: viewController as! SLOnboardingViewController),
            index < self.onboardingControllers.count - 1    
        {
            return self.onboardingControllers[index + 1]
        }
        
        return nil
    }
    
    public func pageViewController(
        _ pageViewController: UIPageViewController,
        viewControllerBefore viewController: UIViewController
        ) -> UIViewController?
    {
        if let index = self.onboardingControllers.index(of: viewController as! SLOnboardingViewController), index > 0 {
            return self.onboardingControllers[index - 1]
        }
        
        return nil
    }

    
    public func pageViewController(
        _ pageViewController: UIPageViewController,
        didFinishAnimating finished: Bool,
                           previousViewControllers: [UIViewController],
                           transitionCompleted completed: Bool
        )
    {
        if !completed {
            return
        }
        
        guard let previousViewController = previousViewControllers.first as? SLOnboardingViewController else {
            return
        }
        
        if let fromIndex = self.onboardingControllers.index(of: previousViewController) {
            let diff = self.toViewControllerIndex - fromIndex
            if (diff > 0 && self.toViewControllerIndex < self.onboardingControllers.count) ||
                (diff < 0 && self.toViewControllerIndex >= 0)
            {
                self.pageControl.currentPage = self.toViewControllerIndex
            }
        }
    }
    
    public func pageViewController(
        _ pageViewController: UIPageViewController,
        willTransitionTo pendingViewControllers: [UIViewController]
        )
    {
        guard let viewController = pendingViewControllers.first as? SLOnboardingViewController else {
            return
        }
        
        if let index = self.onboardingControllers.index(of: viewController) {
            self.toViewControllerIndex = index
        }
    }
}
