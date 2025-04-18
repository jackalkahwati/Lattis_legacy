//
//  OnboardingViewController.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/9/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Pageboy
import Localize_Swift
import Cartography

class OnboardingViewController: PageboyViewController {
    
    fileprivate let startButton = UIButton()
    fileprivate let pageControl = UIPageControl()
    
    fileprivate let pages: [OnboardingPage] = [
        OnboardingPage(.init(image: UIImage(named: "walkthrough_theft_alerts")!, title: "walkthrough_theft_alerts_title".localized(), subtitle: "walkthrough_theft_alerts_description".localized())),
        OnboardingPage(.init(image: UIImage(named: "walkthrough_crash_detect")!, title: "walkthrough_crash_alerts_title".localized(), subtitle: "walkthrough_crash_alerts_description".localized())),
        OnboardingPage(.init(image: UIImage(named: "walkthrough_unlock")!, title: "walkthrough_tap_to_unlock_title".localized(), subtitle: "walkthrough_tap_to_unlock_description".localized())),
        OnboardingPage(.init(image: UIImage(named: "walkthrough_map")!, title: "walkthrough_locate_bike_title".localized(), subtitle: "walkthrough_locate_bike_description".localized()))]
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        view.backgroundColor = .elSlateGrey
        configureButton()
        
        self.delegate = self
        self.dataSource = self
    }
    
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .lightContent
    }
    
    fileprivate func configureButton() {
        view.addSubview(pageControl)
        view.addSubview(startButton)
        startButton.addTarget(self, action: #selector(start(_:)), for: .touchUpInside)
        let margin: CGFloat = 20
        bigRoundCorners(startButton)
        startButton.backgroundColor = .elDarkSkyBlue
        startButton.setTitle("get_started_btn".localized().lowercased().capitalized, for: .normal)
        constrain(startButton, pageControl, view) { button, pages, superview in
            button.centerX == superview.centerX
            button.left >= superview.left + margin ~ .defaultLow
            button.right >= superview.right - margin ~ .defaultLow
            button.bottom == superview.safeAreaLayoutGuide.bottom - margin
            pages.center == button.center
        }
    
        pageControl.currentPageIndicatorTintColor = .elDarkSkyBlue
        pageControl.pageIndicatorTintColor = .elSteel
        pageControl.isUserInteractionEnabled = false
        pageControl.numberOfPages = 4
        startButton.isHidden = true
    }
    
    @objc func start(_ sender: Any) {
        navigationController?.setViewControllers([LogInRouter.welcome], animated: true)
    }
    
    fileprivate var isStartHidden: Bool = true {
        didSet {
            guard isStartHidden != oldValue else { return }
            if isStartHidden {
                UIView.animate(withDuration: 0.3, animations: {
                    self.startButton.alpha = 0
                }) { (_) in
                    self.startButton.isHidden = true
                }
            } else {
                self.startButton.alpha = 0
                self.startButton.isHidden = false
                UIView.animate(withDuration: 0.3, animations: {
                    self.startButton.alpha = 1
                })
            }
        }
    }
}

extension OnboardingViewController: PageboyViewControllerDelegate, PageboyViewControllerDataSource {
    func pageboyViewController(_ pageboyViewController: PageboyViewController, didReloadWith currentViewController: UIViewController, currentPageIndex: PageboyViewController.PageIndex) {
        
    }
    
    func pageboyViewController(_ pageboyViewController: PageboyViewController, didScrollTo position: CGPoint, direction: PageboyViewController.NavigationDirection, animated: Bool) {
        
    }
    
    func numberOfViewControllers(in pageboyViewController: PageboyViewController) -> Int {
        return pages.count
    }
    
    func viewController(for pageboyViewController: PageboyViewController, at index: PageboyViewController.PageIndex) -> UIViewController? {
        return pages[index]
    }
    
    func defaultPage(for pageboyViewController: PageboyViewController) -> PageboyViewController.Page? {
        return nil
    }
    
    func pageboyViewController(_ pageboyViewController: PageboyViewController, didCancelScrollToPageAt index: PageboyViewController.PageIndex, returnToPageAt previousIndex: PageboyViewController.PageIndex) {
        
    }
    
    func pageboyViewController(_ pageboyViewController: PageboyViewController, willScrollToPageAt index: PageboyViewController.PageIndex, direction: PageboyViewController.NavigationDirection, animated: Bool) {
        
    }
    
    func pageboyViewController(_ pageboyViewController: PageboyViewController, didScrollToPageAt index: PageboyViewController.PageIndex, direction: PageboyViewController.NavigationDirection, animated: Bool) {
        pageControl.currentPage = index
        isStartHidden = index < pages.count - 1
//        if index == pages.count - 1 && startButton.isHidden {
//            startButton.isHidden = false
//        } else {
//            startButton.isHidden = true
//        }
    }
}
