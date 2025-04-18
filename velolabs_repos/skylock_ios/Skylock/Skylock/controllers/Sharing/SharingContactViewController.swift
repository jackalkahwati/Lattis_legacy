//
//  SharingContactsViewController.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 14/01/2017.
//  Copyright © 2017 Andre Green. All rights reserved.
//


class SharingContactsViewController: ContactsViewController {
    fileprivate let lockId: Int32
    init(lockId: Int32) {
        self.lockId = lockId
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        title = "SHARING".localized()
    }
    
    override func didSelect(contact: EllipseContact, at indexPath: IndexPath) {
        let slvc = SLShareInviteViewController(lockId: lockId, contact: contact)
        navigationController?.pushViewController(slvc, animated: true)
    }
}



