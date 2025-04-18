//
//  MenuMenuViewController.swift
//  Lattis
//
//  Created by Ravil Khusainov on 27/02/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit
import Oval

class MenuViewController: ViewController {
    @IBOutlet weak var helpLabel: UILabel!
    @IBOutlet weak var logoutLabel: UILabel!
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var tableView: UITableView!
    
    var interactor: MenuInteractorInput!
    
    fileprivate var actions: [Action] = []
    fileprivate var selectedIndex = 0

    override func viewDidLoad() {
        super.viewDidLoad()
        
        title = "menu_title".localized()
        
        tableView.dataSource = self
        tableView.delegate = self
        tableView.tableFooterView = UIView()
    }
    
    override var prefersStatusBarHidden: Bool {
        return true
    }
    
    override var title: String? {
        didSet {
            titleLabel.text = title?.uppercased()
        }
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        interactor.viewLoaded()
    }
    
    @IBAction func help(_ sender: Any) {
        interactor.help()
    }
    
    @IBAction func logout(_ sender: Any) {
        func performAction() {
            AppRouter.shared.postCancelBooking = nil
            let alert = ActionAlertView.alert(title: "menu_logout_title".localized(), subtitle: "menu_logout_text".localized())
            alert.action = AlertAction(title: "menu_logout_confirm".localized(), action: AppDelegate.shared.logout)
            alert.cancel = AlertAction(title: "menu_logout_cancel".localized(), action: {})
            alert.show()
        }
        if AppRouter.shared.isTripStarted {
            let alert = ActionAlertView.alert(title: "logout_ride_warning_title".localized(), subtitle: "logout_ride_warning_text".localized())
            alert.action = AlertAction(title: "logout_ride_warning_action".localized(), action: {
                AppRouter.shared.tripEnded = AppDelegate.shared.logout
                self.interactor.home()
            })
            alert.cancel = AlertAction(title: "logout_ride_warning_cancel".localized(), action: {})
            alert.show()
            return
        }
        if let cancel = AppRouter.shared.cancelBooking {
            AppRouter.shared.postCancelBooking = performAction
            interactor.home()
            cancel(false)
            return
        }
        performAction()
    }
}

extension MenuViewController: MenuInteractorOutput {
    func updateMenu(withDamage enabled: Bool) {
        actions = [
            Action(name: "menu_home".localized(), image: #imageLiteral(resourceName: "icon_menu_home"), action: interactor.home),
            Action(name: "menu_profile".localized(), image: #imageLiteral(resourceName: "icon_menu_profile"), action: interactor.profile),
            Action(name: "payment_title".localized(), image: #imageLiteral(resourceName: "icon_menu_billing"), action: interactor.billing)
        ]
//        #if DEV || DEBUG
        actions.append(Action(name: "menu_history".localized(), image: #imageLiteral(resourceName: "icon_menu_history"), action: interactor.history))
//        #endif
        if enabled {
            actions.append(Action(name: "menu_damage".localized(), image: #imageLiteral(resourceName: "icon_menu_damage_report"), action: interactor.damage))
            actions.append(Action(name: "menu_theft".localized(), image: #imageLiteral(resourceName: "icon_theft_menu"), action: interactor.theft))
        }
        tableView.reloadData()
    }
    
    func reload(selected: Int) {
        selectedIndex = selected
        tableView.reloadData()
    }
}

extension MenuViewController: UITableViewDataSource, UITableViewDelegate {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return actions.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: MenuCell.identifier, for: indexPath) as! MenuCell
        cell.action = actions[indexPath.row]
        if cell.isSelected == false && selectedIndex == indexPath.row {
            tableView.selectRow(at: indexPath, animated: false, scrollPosition: .none)
        }
        cell.setSelected(indexPath.row == selectedIndex, animated: false)
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        if indexPath.row < 2 {
            selectedIndex = indexPath.row
        }
        let cell = tableView.cellForRow(at: indexPath) as? MenuCell
        cell?.action?.action()
    }
}

extension MenuViewController {
    struct Action {
        let name: String
        let image: UIImage
        let action: () -> ()
    }
}


