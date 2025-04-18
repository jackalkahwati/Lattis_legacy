//
//  MembershipLogicController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 03.08.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import Foundation
import Model

enum MembershipState {
    case reload
    case failure(Error)
    case delete([IndexPath])
    case insert([IndexPath])
}

final class MembershipLogicController {

    let sections: [SectionInfo] = [.subsctiptions, .memberships]
    var subsHidden = true {
        didSet {
            guard subsHidden != oldValue else { return }
            let paths = (1..<subscriptions.count).map{IndexPath(row: $0, section: 0)}
            if subsHidden {
                stateHandler(.delete(paths))
            } else {
                stateHandler(.insert(paths))
            }
        }
    }
    var search: () -> Void = {}
    fileprivate var stateHandler: (MembershipState) -> Void = {_ in}
    fileprivate(set) var memberships: [Membership] = []
    fileprivate(set) var subscriptions: [Subscription] = [] { didSet { calculate() }}
    fileprivate let network: SubscriptionsAPI & UserAPI = AppRouter.shared.api()
    
    func fetch(completion: @escaping (MembershipState) -> Void) {
        network.fetchMemberships { [weak self] (result) in
            switch result {
            case .failure(let error):
                completion(.failure(error))
            case .success(let memberships):
                self?.memberships = memberships
                self?.calculate()
                completion(.reload)
            }
        }
        network.fetchSubscriptions { [weak self] (result) in
            switch result {
            case .failure(let error):
                completion(.failure(error))
            case .success(let subscriptions):
                self?.subscriptions = subscriptions
                completion(.reload)
            }
        }
        stateHandler = completion
    }
    
    func numberOfRows(in section: Int) -> Int {
        if section == 0 {
            if subsHidden && subscriptions.count > 1 {
                return 1
            }
            return subscriptions.count
        }
        return memberships.count
    }
    
    func actionForHeader(in section: Int) -> ActionInfo {
        var title = sections[section].title
        if section == 0 {
            title += " (\(subscriptions.count))"
            if subscriptions.count <= 1 { return .passive(title) }
            func actionTitle() -> String { subsHidden ? "show_all".localized() : "hide".localized() }
            return .init(title: title, actionTitle: actionTitle(), actionIcon: nil, action: { [unowned self] in
                self.subsHidden = !self.subsHidden
                return actionTitle()
            })
        }
        return .init(title: title, actionTitle: nil, actionIcon: .named("icon_search")) { [unowned self] in
            self.search()
            return nil
        }
    }
    
    fileprivate func calculate() {
        memberships = memberships.filter({ mem in
            return !self.subscriptions.contains(where: {
                $0.membership.id == mem.id
            })
        })
    }
    
    struct SectionInfo {
        let cellIdentifier: String
        let title: String
        
        static let memberships = SectionInfo(cellIdentifier: "cell_membership", title: "available_memberships".localized())
        static let subsctiptions = SectionInfo(cellIdentifier: "cell_membership", title: "your_memberships".localized())
    }
    
    struct ActionInfo {
        let title: String
        let actionTitle: String?
        let actionIcon: UIImage?
        let action: (() -> String?)?
        
        static func passive(_ title: String) -> ActionInfo {
            .init(title: title, actionTitle: nil, actionIcon: nil, action: nil)
        }
    }
}
