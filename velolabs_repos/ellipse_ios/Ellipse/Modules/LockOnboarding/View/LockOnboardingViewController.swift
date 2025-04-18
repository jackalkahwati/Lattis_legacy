//
//  LockOnboardingLockOnboardingViewController.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 16/10/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Pageboy

class LockOnboardingViewController: PageboyViewController {
    var interactor: LockOnboardingInteractorInput!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        view.backgroundColor = .white
        navigationItem.leftBarButtonItem = UIBarButtonItem.close(target: self, action: #selector(close))
        title = "addlock_home_description_1".localized()
        
        dataSource = self
        delegate = self
        isScrollEnabled = false
    }
    
    @objc func close() {
        interactor.delegate?.didFinishLockOnboarding()
        dismiss(animated: true, completion: nil)
    }
}

extension LockOnboardingViewController: LockOnboardingInteractorOutput {
    func show(title: String?) {
        self.title = title
    }
    
    func pushPage() {
        scrollToPage(.next, animated: true)
    }
    
    func reload() {
        reloadData()
    }
    
    func hideCloseButton() {
        navigationItem.leftBarButtonItem = nil
    }
}

extension LockOnboardingViewController: PageboyViewControllerDataSource, PageboyViewControllerDelegate {
    func pageboyViewController(_ pageboyViewController: PageboyViewController, didReloadWith currentViewController: UIViewController, currentPageIndex: PageboyViewController.PageIndex) {
        
    }
    
    func pageboyViewController(_ pageboyViewController: PageboyViewController, didScrollTo position: CGPoint, direction: PageboyViewController.NavigationDirection, animated: Bool) {
        
    }
    
    func numberOfViewControllers(in pageboyViewController: PageboyViewController) -> Int {
        return interactor.numberOfPages
    }
    
    func viewController(for pageboyViewController: PageboyViewController, at index: PageboyViewController.PageIndex) -> UIViewController? {
        return interactor.page(for: index) as? UIViewController
    }
    
    func defaultPage(for pageboyViewController: PageboyViewController) -> PageboyViewController.Page? {
        return nil
    }
    
    func pageboyViewController(_ pageboyViewController: PageboyViewController, willScrollToPageAt index: PageboyViewController.PageIndex, direction: PageboyViewController.NavigationDirection, animated: Bool) {
        
    }
    
    func pageboyViewController(_ pageboyViewController: PageboyViewController, didCancelScrollToPageAt index: PageboyViewController.PageIndex, returnToPageAt previousIndex: PageboyViewController.PageIndex) {
        
    }
    
    func pageboyViewController(_ pageboyViewController: PageboyViewController, didScrollToPageAt index: PageboyViewController.PageIndex, direction: PageboyViewController.NavigationDirection, animated: Bool) {
        interactor.didShowPage(at: index)
    }
}
