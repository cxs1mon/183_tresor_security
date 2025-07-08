import React, {useRef, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {postUser} from "../../comunication/FetchUser";
import ReCAPTCHA from 'react-google-recaptcha';

/**
 * RegisterUser
 * @author Peter Rutschmann
 */
function RegisterUser({loginValues, setLoginValues}) {
    const initialState = {
        firstName: '', lastName: '', email: '', password: '', passwordConfirmation: ''
    };

    const navigate = useNavigate();
    const [captchaToken, setCaptchaToken] = useState('');
    const [credentials, setCredentials] = useState(initialState);
    const [errorMessage, setErrorMessage] = useState('');

    const isPasswordStrong = password => {
        if (!password || password.length < 8) return false;
        if (!/[A-Z]/.test(password)) return false;
        if (!/[a-z]/.test(password)) return false;
        if (!/[0-9]/.test(password)) return false;
        if (!/[!@#$%^&*]/.test(password)) return false;
        return true;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrorMessage('');

        //validate
        if (credentials.password !== credentials.passwordConfirmation) {
            console.log("password != passwordConfirmation");
            setErrorMessage('Password and password-confirmation are not equal.');
            return;
        }

        if (!isPasswordStrong(credentials.password)) {
            setErrorMessage('Password must be at least 8 characters long, include uppercase, lowercase, a digit and a special character (!@#$%^&*).');
            return;
        }

        if (!captchaToken) {
            setErrorMessage('Bitte Captcha bestätigen.');
            return;
        }
        console.log('=== DEBUG: Captcha-Token vor dem Senden ===', captchaToken);

        const payload = {
            ...credentials, captchaResponse: captchaToken
        };

        try {
            await postUser(payload);

            setLoginValues({
                userName: credentials.email, password: credentials.password
            });
            setCredentials(initialState);
            navigate('/');
        } catch (error) {
            console.error('Failed to fetch to server:', error.message);
            setErrorMessage(error.message);
        }
    };
    return (<div>
            <script src="https://www.google.com/recaptcha/api.js" async defer></script>
            <h2>Register user</h2>
            <form onSubmit={handleSubmit}>
                <section>
                    <aside>
                        <div>
                            <label>Firstname:</label>
                            <input
                                type="text"
                                value={credentials.firstName}
                                onChange={(e) => setCredentials(prevValues => ({
                                    ...prevValues,
                                    firstName: e.target.value
                                }))}
                                required
                                placeholder="Please enter your firstname *"
                            />
                        </div>
                        <div>
                            <label>Lastname:</label>
                            <input
                                type="text"
                                value={credentials.lastName}
                                onChange={(e) => setCredentials(prevValues => ({
                                    ...prevValues,
                                    lastName: e.target.value
                                }))}
                                required
                                placeholder="Please enter your lastname *"
                            />
                        </div>
                        <div>
                            <label>Email:</label>
                            <input
                                type="email"
                                value={credentials.email}
                                onChange={(e) => setCredentials(prevValues => ({...prevValues, email: e.target.value}))}
                                required
                                placeholder="Please enter your email"
                            />
                        </div>
                    </aside>
                    <aside>
                        <div>
                            <label>Password:</label>
                            <input
                                type="password"
                                value={credentials.password}
                                onChange={(e) => setCredentials(prevValues => ({
                                    ...prevValues,
                                    password: e.target.value
                                }))}
                                required
                                placeholder="Please enter your pwd *"
                            />
                        </div>
                        <div>
                            <label>Password confirmation:</label>
                            <input
                                type="password"
                                value={credentials.passwordConfirmation}
                                onChange={(e) => setCredentials(prevValues => ({
                                    ...prevValues, passwordConfirmation: e.target.value
                                }))}
                                required
                                placeholder="Please confirm your pwd *"
                            />
                        </div>
                    </aside>
                </section>
                <ReCAPTCHA
                    sitekey='6LcjDHorAAAAAPdlSXZFOa8SWAM60FkFHIjl_B3j'
                    onChange={token => setCaptchaToken(token)}
                />
                <button type="submit">Register</button>
                {errorMessage && <p style={{color: 'red'}}>{errorMessage}</p>}
            </form>
        </div>

    );
}

export default RegisterUser;
