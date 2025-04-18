//
//  AxaLockControl.swift
//  AXALock
//
//  Created by Ravil Khusainov on 30.03.2020.
//

import UIKit

fileprivate let defaultSize = CGSize(width: 88, height: 48)
fileprivate let focusDiametr = CGFloat(32)

public class AxaLockControl: UIControl {
    
    public fileprivate(set) var isLocked: Bool = true
    public var isProcessing: Bool = false
    
    fileprivate let focusView = UIView()
    fileprivate let distance: CGFloat
    fileprivate let lockedImageView: UIImageView
    fileprivate let unlockedImageView: UIImageView
    fileprivate var centerXConstraint: NSLayoutConstraint!
    fileprivate let lockedColor: UIColor
    fileprivate let unlockedColor: UIColor
    
    public init(unlockedColor: UIColor = .black, lockedColor: UIColor = .red, lockedImage: UIImage? = nil, unlockedImage: UIImage? = nil) {
        distance = defaultSize.width/2 - focusDiametr/2 - (defaultSize.height - focusDiametr)/2
        lockedImageView = UIImageView(image: lockedImage)
        unlockedImageView = UIImageView(image: unlockedImage)
        self.unlockedColor = unlockedColor
        self.lockedColor = lockedColor
        super.init(frame: .init(origin: .zero, size: defaultSize))
        translatesAutoresizingMaskIntoConstraints = false
        self.backgroundColor = lockedColor
        layer.cornerRadius = defaultSize.height/2
        
        focusView.translatesAutoresizingMaskIntoConstraints = false
        focusView.isUserInteractionEnabled = false
        focusView.backgroundColor = .white
        addSubview(focusView)
        focusView.layer.cornerRadius = focusDiametr/2
        
        unlockedImageView.alpha = 0
        
        addSubview(lockedImageView)
        addSubview(unlockedImageView)
        lockedImageView.translatesAutoresizingMaskIntoConstraints = false
        unlockedImageView.translatesAutoresizingMaskIntoConstraints = false
        
        if #available(iOS 9.0, *) {
            centerXConstraint = focusView.centerXAnchor.constraint(equalTo: self.centerXAnchor, constant: distance)
            var constraints = [
                self.widthAnchor.constraint(equalToConstant: defaultSize.width),
                self.heightAnchor.constraint(equalToConstant: defaultSize.height),
                focusView.widthAnchor.constraint(equalToConstant: focusDiametr),
                focusView.heightAnchor.constraint(equalToConstant: focusDiametr),
                focusView.centerYAnchor.constraint(equalTo: self.centerYAnchor),
                centerXConstraint!
            ]
            if let image = unlockedImage {
                constraints += [
                    unlockedImageView.widthAnchor.constraint(equalToConstant: image.size.width),
                    unlockedImageView.heightAnchor.constraint(equalToConstant: image.size.height),
                    unlockedImageView.centerYAnchor.constraint(equalTo: self.centerYAnchor),
                    unlockedImageView.centerXAnchor.constraint(equalTo: self.centerXAnchor, constant: -distance),
                ]
            }
            if let image = lockedImage {
                constraints += [
                    lockedImageView.widthAnchor.constraint(equalToConstant: image.size.width),
                    lockedImageView.heightAnchor.constraint(equalToConstant: image.size.height),
                    lockedImageView.centerYAnchor.constraint(equalTo: self.centerYAnchor),
                    lockedImageView.centerXAnchor.constraint(equalTo: self.centerXAnchor, constant: distance)
                ]
            }
            NSLayoutConstraint.activate(constraints)
        } else {
            // Fallback on earlier versions
        }
        addTarget(self, action: #selector(toggle), for: .touchUpInside)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func sizeThatFits(_ size: CGSize) -> CGSize {
        defaultSize
    }
    
    @objc
    public func toggle() {
        guard !isProcessing else { return }
        set(isLocked: !isLocked)
    }
    
    public func set(isLocked: Bool, animated: Bool = true) {
        guard self.isLocked != isLocked else { return }
        isProcessing = false
        centerXConstraint.constant = distance * (isLocked ? 1 : -1)
        let complete: () -> () = { [weak self] in
            self?.isLocked = isLocked
            self?.sendActions(for: .valueChanged)
            self?.isUserInteractionEnabled = true
        }
        if animated {
            isUserInteractionEnabled = false
            UIView.animate(withDuration: 0.3, delay: 0, options: .curveEaseInOut, animations: {
                self.layoutIfNeeded()
                self.lockedImageView.alpha = isLocked ? 1 : 0
                self.unlockedImageView.alpha = isLocked ? 0 : 1
                self.backgroundColor = isLocked ? self.lockedColor : self.unlockedColor
            }, completion: { _ in
                complete()
            })
        } else {
            layoutIfNeeded()
            self.lockedImageView.alpha = isLocked ? 1 : 0
            self.unlockedImageView.alpha = isLocked ? 0 : 1
            self.backgroundColor = isLocked ? self.lockedColor : self.unlockedColor
            complete()
        }
    }
}

