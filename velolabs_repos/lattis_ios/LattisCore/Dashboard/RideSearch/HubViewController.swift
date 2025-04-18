//
//  HubViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 15.01.2021.
//  Copyright © 2021 Lattis inc. All rights reserved.
//

import UIKit
import Model
import Cartography

class HubViewController: UIViewController {

    fileprivate let hub: Hub
    fileprivate let bikes: [Bike]
    fileprivate let callback: (Bike) -> Void
    fileprivate let tableView = UITableView()
    fileprivate weak var logic: RideSearchLogicController?
    fileprivate weak var selection: HubSelectionViewController?
    
    init(_ hub: Hub, logic: RideSearchLogicController, completion: @escaping (Bike) -> Void) {
        self.hub = hub
        self.callback = completion
        self.logic = logic
        self.bikes = logic.bikes(for: hub)
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()

        if #available(iOS 13.0, *) {
            overrideUserInterfaceStyle = .light
        }
        view.backgroundColor = .white
        
        addCloseButton()
        
        title = hub.hubName + " (\(hub.bikes?.count ?? 0))"
        
        view.addSubview(tableView)
        
        constrain(tableView, view) { table, view in
            table.edges == view.edges
        }
        tableView.register(HubBikeCell.self, forCellReuseIdentifier: "bike")
        tableView.rowHeight = UITableView.automaticDimension
        tableView.estimatedRowHeight = 88
        tableView.tableFooterView = UIView()
        tableView.separatorInset = .init(top: 0, left: .margin, bottom: 0, right: .margin)
        tableView.dataSource = self
        tableView.delegate = self
    }
    
    fileprivate func info(bike: Bike) {
        let details = BikeDetailsViewController(bike)
        details.closeButton.addTarget(self, action: #selector(close), for: .touchUpInside)
        present(details, animated: true, completion: nil)
    }
    
    fileprivate func select(bike: Bike) {
        let actionContainer = ActionContainer(left: .plain(title: "cancel".localized(), style: .plain, handler: { [weak self] in
            self?.cancelSelection()
        }), right: .plain(title: "reserve".localized(), handler: { [weak self] in
            self?.reserveSelected()
        }), priority: .right)
        let controller = HubSelectionViewController(bike: bike, actions: actionContainer)
        controller.modalTransitionStyle = .crossDissolve
        controller.modalPresentationStyle = .overCurrentContext
        present(controller, animated: true)
        selection = controller
    }
    
    fileprivate func cancelSelection() {
        dismiss(animated: true)
    }
    
    fileprivate func reserveSelected() {
        guard let bike = selection?.bike else { return }
        Analytics.log(.reserve())
        if let phone = bike.requirePhoneNumber, phone, !logic!.hasPhonePumber {
            let alert = AlertController(title: "label_note".localized(), message: .plain("mandatory_phone_text".localized()))
            alert.actions = [
                .plain(title: "mandatory_phone_action".localized()) {
                    self.present(.navigation(ProfileViewController(true)), animated: true, completion: nil)
                },
                .cancel
            ]
            selection?.present(alert, animated: true, completion: nil)
            return
        }
        dismiss(animated: false) {
            self.dismiss(animated: true) {
                self.logic?.selected = bike
                self.callback(bike)
            }
        }
    }
}

extension HubViewController: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        bikes.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "bike", for: indexPath) as! HubBikeCell
        let bike = bikes[indexPath.row]
        cell.update(bike: bike) { [unowned self] in
            self.info(bike: bike)
        } select: { [unowned self] in
            self.select(bike: bike)
        }

        return cell
    }
}

final class HubSelectionViewController: UIViewController {
    
    let bike: Bike
    let actions: ActionContainer
    let confimation: RideConfirmationViewController
    
    init(bike: Bike, actions: ActionContainer) {
        self.bike = bike
        self.actions = actions
        self.confimation = .init(bike, disconut: nil, pricing: nil)
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        if #available(iOS 13.0, *) {
            overrideUserInterfaceStyle = .light
        }
        view.backgroundColor = UIColor(white: 0, alpha: 0.5)
        
        let contentView = UIView()
        contentView.backgroundColor = .white
        contentView.layer.cornerRadius = .containerCornerRadius
        contentView.layer.maskedCorners = [.layerMinXMinYCorner, .layerMaxXMinYCorner]
        
        view.addSubview(contentView)
        
        contentView.addSubview(actions)
        
        confimation.willMove(toParent: self)
        addChild(confimation)
        contentView.addSubview(confimation.view)
        confimation.didMove(toParent: self)
        
        constrain(contentView, confimation.view, actions, view) { content, confirmation, actions, view in
            content.bottom == view.bottom
            content.left == view.left
            content.right == view.right
            
            actions.bottom == content.safeAreaLayoutGuide.bottom - .margin
            actions.left == content.left + .margin
            actions.right == content.right - .margin
            
            confirmation.bottom == actions.top - .margin
            confirmation.left == content.left
            confirmation.right == content.right
            confirmation.top == content.top + .containerCornerRadius
        }
    }
}



