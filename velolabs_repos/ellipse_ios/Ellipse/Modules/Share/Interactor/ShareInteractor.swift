//
//  ShareShareInteractor.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 07/11/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation
import Oval
import LattisSDK
import PhoneNumberKit

fileprivate let hintIsShownKey = "hintIsShown"

class ShareInteractor {
    weak var view: ShareInteractorOutput! {
        didSet {
            errorHandler = ErrorHandler(view)
        }
    }
    var router: ShareRouter!
    
    fileprivate let storage: EllipseStorage = CoreDataStack.shared
    fileprivate var locks: [Ellipse.Shared] = []
    fileprivate var storageHandler: StorageHandler?
    fileprivate let network: LocksNetwork = Session.shared
    fileprivate var errorHandler: ErrorHandler!
    fileprivate var ellipseToShare: Ellipse?
    fileprivate var ellipseToBlink: Ellipse?
    
    fileprivate var hintIsShown: Bool {
        set {
            UserDefaults.standard.set(newValue, forKey: hintIsShownKey)
            UserDefaults.standard.synchronize()
        }
        get {
            return UserDefaults.standard.bool(forKey: hintIsShownKey)
        }
    }
}

extension ShareInteractor: ShareInteractorInput {
    func start() {
        let userId = User.currentId
        if hintIsShown == false {
            hintIsShown = true
            view.showHint()
        }
        storageHandler = storage.ellipses { [unowned self] (ellipses) in
            self.locks = ellipses.filter({ $0.owner?.userId == userId }).map(Ellipse.Shared.init)
            self.view.refresh()
            self.view.setEmpty(hidden: self.locks.isEmpty == false)
        }
    }
    
    var numberOfSections: Int {
        return 1
    }
    
    func item(for indexPath: IndexPath) -> Ellipse.Shared {
        return locks[indexPath.row]
    }
    
    func numberOfRows(in section: Int) -> Int {
        return locks.count
    }
    
    func share(ellipse: Ellipse) {
        func share() {
            ellipseToShare = ellipse
            router.openContacts(delegate: self)
        }
        if ellipse.isShared == false && ellipse.borrower != nil {
//            unshare(ellipse: ellipse) {
//                share()
//            }
        } else {
            share()
        }
    }
    
    func unshare(ellipse: Ellipse) {
//        let action = AlertView.Action(title: "unshare".localized()) { (_) in
            self.view.startLoading(text: "unsharing".localized())
        self.network.unshare(lock: ellipse) { [weak self] result in
            switch result {
            case .success:
                self?.view.stopLoading(completion: nil)
                var el = ellipse
                el.borrower = nil
                el.sharedToUserId = nil
                el.shareId = nil
                self?.storage.save(el)
                log(.share(.unshared))
            case .failure(let error):
                self?.errorHandler.handle(error: error)
            }
        }
//        }
//        AlertView.alert(title: "confirm_action".localized(), text: "unshare_alert_text".localized(), actions: [.cancel, action]).show()
    }
    
    func blink(ellipse: Ellipse) {
        ellipseToBlink = ellipse
        view.startLoading(text: "blinking_led".localized())
        EllipseManager.shared.scan(with: self)
    }
    
    func addNew() {
        router.openOnboarding()
    }
}

extension ShareInteractor: ContactsInteractorDelegate {
    func didSelect(contact: Contact) {
        guard let ellipse = ellipseToShare else { return }
        func send(number: String) {
            view.startLoading(text: "sending_invitation".localized())
            var con = contact
            con.update(primary: number.internationalPhoneNumberFormat)
            network.share(lock: ellipse.lockId, to: con) { [weak self] result in
                switch result {
                case .success:
                    self?.view.show(warning: "share_invitation_sent_text".localized() , title: "share_invitation_sent".localized())
                    log(.share(.shared))
                case .failure(let error):
                    self?.errorHandler.handle(error: error)
                }
            }
        }
        func alert(number: String) {
            let share = AlertView.Action(title: "send".localized().lowercased().capitalized) { (_) in
                send(number: number)
            }
            AlertView.alert(title: "share_to".localizedFormat(ellipse.name ?? "No name", contact.fullName, number), text: "sharing_invite_hint".localized(), actions: [.cancel, share]).show()
        }
        router.dismiss { [unowned self] in
            if contact.phoneNumbers.count > 1 {
                self.router.chooseNumber(contact: contact, completion: { (number) in
                    alert(number: number)
                })
            } else if let number = contact.phoneNumbers.first {
                alert(number: number)
            } else {
                // TODO: show error
            }
        }
    }
    
    func didSelect(contacts: [Contact]) {}
}

extension ShareInteractor: EllipseManagerDelegate {
    func manager(_ lockManager: EllipseManager, didUpdateLocks insert: [LattisSDK.Ellipse], delete: [LattisSDK.Ellipse]) {
        if let blink = ellipseToBlink, let lock = lockManager.locks.filter({$0.macId == blink.macId}).first {
            lockManager.stopScan()
            lock.flashLED() { _ in
                self.view.stopLoading(completion: nil)
            }
        }
    }
}

extension Ellipse {
    class Shared {
        let ellipse: Ellipse
        var contact: Contact? = nil
        
        init(_ ellipse: Ellipse) {
            self.ellipse = ellipse
        }
    }
}

extension String {
    var internationalPhoneNumberFormat: String {
        let kit = PhoneNumberKit()
        guard let phone = try? kit.parse(self, ignoreType: true) else { return self }
        return "+\(phone.countryCode)\(phone.nationalNumber)"
    }
}
