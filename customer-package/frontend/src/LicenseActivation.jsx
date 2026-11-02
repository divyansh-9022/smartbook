import { useEffect, useState } from 'react'

const API_URL = import.meta.env.VITE_API_URL || (import.meta.env.DEV ? 'http://localhost:8080' : '')

function LicenseActivation({ token, onActivated }) {
  const [licenseKey, setLicenseKey] = useState('')
  const [activated, setActivated] = useState(false)
  const [loading, setLoading] = useState(true)
  const [activating, setActivating] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  useEffect(() => {
    const checkLicense = async () => {
      try {
        const response = await fetch(
          `${API_URL}/api/license/status`,
          {
            headers: {
              Authorization: `Bearer ${token}`
            }
          }
        )

        if (!response.ok) {
          throw new Error('Unable to check licence status.')
        }

        const data = await response.json()
        setActivated(data.activated)

      } catch (err) {
        setError(err.message || 'Unable to check licence status.')
      } finally {
        setLoading(false)
      }
    }

    checkLicense()
  }, [token])

  const handleActivate = async (event) => {
    event.preventDefault()

    setError('')
    setMessage('')

    if (!licenseKey.trim()) {
      setError('Please enter your Gumroad licence key.')
      return
    }

    setActivating(true)

    try {
      const response = await fetch(
        `${API_URL}/api/license/activate`,
        {
          method: 'POST',
          headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            licenseKey: licenseKey.trim()
          })
        }
      )

      const data = await response.json().catch(() => ({}))

      if (!response.ok) {
        throw new Error(
          data.detail ||
          data.message ||
          'Licence activation failed.'
        )
      }
  if (onActivated) {
    onActivated(data)
  }

      setActivated(true)
      setMessage(data.message || 'SmartBook activated successfully!')
      setLicenseKey('')

    } catch (err) {
      setError(err.message || 'Unable to activate licence.')
    } finally {
      setActivating(false)
    }
  }

  if (loading) {
    return <p>Checking SmartBook licence...</p>
  }

  return (
    <section className="license-activation-section">

      <h3>SmartBook Software Licence</h3>

      <p>
        Activate SmartBook using the licence key
        provided after your Gumroad purchase.
      </p>

      {activated ? (
        <div className="license-active">
          <h4>✓ SmartBook Licence Activated</h4>
          <p>Your licence has been verified successfully.</p>
        </div>
      ) : (
        <form onSubmit={handleActivate}>

          <label>Gumroad Licence Key</label>

          <input
            type="text"
            value={licenseKey}
            onChange={event => setLicenseKey(event.target.value)}
            placeholder="Enter your licence key"
            autoComplete="off"
            required
          />

          <button
            type="submit"
            disabled={activating}
          >
            {activating ? 'Verifying...' : 'Activate SmartBook'}
          </button>

        </form>
      )}

      {message && (
        <p className="success-message">{message}</p>
      )}

      {error && (
        <p className="login-error">{error}</p>
      )}

      <p className="license-help">
        Need a licence? Purchase SmartBook through our official
        Gumroad store.
      </p>

    </section>
  )
}

export default LicenseActivation