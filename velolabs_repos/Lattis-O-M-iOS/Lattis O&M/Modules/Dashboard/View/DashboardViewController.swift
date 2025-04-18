//
//  DashboardDashboardViewController.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 08/03/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import LGSideMenuController
import Cartography
import Tabman
import Pageboy

class DashboardViewController: ViewController {
    @IBOutlet weak var createButton: UIBarButtonItem!
    var interactor: DashboardInteractorInput!
    
    fileprivate var pageController: DashboardPageController!
    fileprivate var defaultPage: PageboyViewController.Page? = nil
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        navigationController?.navigationBar.set(style: .blue)
        interactor.viewLoaded()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        navigationController?.isNavigationBarHidden = false
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)

        interactor.viewDidApear()
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if let controller = segue.destination as? DashboardPageController {
            pageController = controller
            pageController.dataSource = self
            pageController.delegate = self
            pageController.bar.items = interactor.tabTitles.map { TabmanBar.Item(title: $0) }
        }
    }
    
    
    @IBAction func menu(_ sender: Any) {
        sideMenuController?.showLeftViewAnimated(sender: self)
    }
    
    @IBAction func addAction(_ sender: Any) {
        pageController.scrollToPage(.next, animated: true)
        interactor.scanQRCode()
    }
}

extension DashboardViewController: DashboardInteractorOutput {
    func change(vendor: Lock.Vendor) {
        defaultPage = .last
        pageController.reloadPages()
    }
}

extension DashboardViewController: PageboyViewControllerDataSource, PageboyViewControllerDelegate {
    func numberOfViewControllers(in pageboyViewController: PageboyViewController) -> Int {
        return interactor.numberOfTabs
    }
    
    func viewController(for pageboyViewController: PageboyViewController, at index: PageboyViewController.PageIndex) -> UIViewController? {
        return interactor.tab(for: index)
    }
    
    func defaultPage(for pageboyViewController: PageboyViewController) -> PageboyViewController.Page? {
        return defaultPage
    }
}

